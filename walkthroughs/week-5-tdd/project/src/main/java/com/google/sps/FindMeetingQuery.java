// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // throw new UnsupportedOperationException("TODO: Implement this method.");

    long duration = request.getDuration();

    // a meeting that is over a day long cannot be scheduled
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return new ArrayList<TimeRange>();
    }

    //  get a list of individuals we need to consider from request
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Collection<String> allAttendees = new ArrayList<String>();
    for (String person : attendees) {
        allAttendees.add(person);
    }
    for (String person : optionalAttendees) {
        allAttendees.add(person);
    }

    // isolate the timeranges from events that pertain to required and optional attendees
    ArrayList<TimeRange> busyTimeRanges = new ArrayList<TimeRange>();
    ArrayList<Event> optionalBusyEvents = new ArrayList<Event>();
    ArrayList<TimeRange> allBusyTimeRanges = new ArrayList<TimeRange>();
    
    // maintain a list of events for each optional attendees
    HashMap<String, HashSet<Event>> optionalAttendeeEvents = new HashMap<>();
    for (Event e : events) {
      for (String a: e.getAttendees()) {
        if (attendees.contains(a)) {
          busyTimeRanges.add(e.getWhen());
        } else if (optionalAttendees.contains(a)) {
          optionalBusyEvents.add(e);
          // builds the hashset that contains each optional attendee and their events
          if (!optionalAttendeeEvents.containsKey(a)) {
            HashSet<Event> temp = new HashSet<Event>();
            temp.add(e);
            optionalAttendeeEvents.put(a, temp);
          } else {
            optionalAttendeeEvents.get(a).add(e);
          }
        }
        if (allAttendees.contains(a)) {
          allBusyTimeRanges.add(e.getWhen());
        }
      }
    }

    // find the TimeRanges that work for employees:
    // first sort then get the intersection
    Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);
    Collections.sort(allBusyTimeRanges, TimeRange.ORDER_BY_START);

    busyTimeRanges = getIntersection(busyTimeRanges);
    ArrayList<TimeRange> allBusyTimeRangesIntersection = getIntersection(allBusyTimeRanges);

    ArrayList<TimeRange> result;
    // if the intersection of all events for both mandatory and optional attendees take up the
    // whole day then just get the free times for the mandatory people
    if (allBusyTimeRangesIntersection.size() == 1 && allBusyTimeRangesIntersection.get(0).equals(TimeRange.WHOLE_DAY)) {
      result = getFreeTime(busyTimeRanges, duration);
    } else {
      result = getFreeTime(allBusyTimeRangesIntersection, duration);
      if (result.size() == 0) { 
        ArrayList<TimeRange> unChangeableTimes = getFreeTime(busyTimeRanges, duration);

        // corner case
        if (unChangeableTimes.size() == 0) {
            return new ArrayList<>();
        }

        TimeRange needToBeFree = findOptimalTime(optionalAttendees, optionalBusyEvents, unChangeableTimes, optionalAttendeeEvents);

        // free up this timerange we found;
        result = removeTimeConflicts(allBusyTimeRanges, optionalAttendeeEvents, needToBeFree, duration);
      }
    }

    return result;
  }

  /* find the intersection of all the non-available time ranges */
  public ArrayList<TimeRange> getIntersection(ArrayList<TimeRange> busyTimeRangesOriginal) {
    // modifies a copy of the original time ranges
    ArrayList<TimeRange> busyTimeRanges = new ArrayList<TimeRange>(busyTimeRangesOriginal);
    int i = 0;
    while (i< busyTimeRanges.size()) {
      // we are at the end of the list or there is no overlap between the current event and the next event
      if (i == busyTimeRanges.size()-1 || !busyTimeRanges.get(i).overlaps(busyTimeRanges.get(i+1))) {
        i++;
      } else {
        // there is an overlap so we remove both events and create a TimeRange that is the intersection of the two
        TimeRange curr = busyTimeRanges.remove(i);
        TimeRange next = busyTimeRanges.remove(i);

        int start = Math.min(curr.start(), next.start());
        int end = Math.max(curr.end(), next.end());
        busyTimeRanges.add(i, TimeRange.fromStartEnd(start, end, false));
      }
    }
    return busyTimeRanges;
  }

  /* find the free time intervals that is equal to or longer than the required duration */
  public ArrayList<TimeRange> getFreeTime(ArrayList<TimeRange> busyTimeRanges, long duration) {
    // find the free part of the day and weed out time ranges thats too short
    ArrayList<TimeRange> result = new ArrayList<TimeRange>();
    int startTime = TimeRange.START_OF_DAY; // end time of the last event we examined
    
    for (int k = 0; k < busyTimeRanges.size(); k++) {
      // case 1: first event of the day starts at the begining of the day
      if (k ==0 && busyTimeRanges.get(k).start() == TimeRange.START_OF_DAY){
        startTime = busyTimeRanges.get(k).end();
        continue;
      }
      // case 2:the normal case
      int endTime = busyTimeRanges.get(k).start();
      if (endTime - startTime >= duration) {
        result.add(TimeRange.fromStartEnd(startTime, endTime, false));     
      } 
      startTime = busyTimeRanges.get(k).end();
    }

    // case 3: the final interval we need to consider at the end of the day
    if (TimeRange.END_OF_DAY - startTime >= duration) {
      result.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }
    return result;
  }

  /* finds and returns the free time range that conflicts with the least number of optional attendees.
  It also removes any entries from the hashmap of optional attendees where the attendee does not 
  have a conflict in the current time range we are freeing */
  public TimeRange findOptimalTime(Collection<String> optionalAttendees, ArrayList<Event> optionalEvents, ArrayList<TimeRange> result, HashMap<String, HashSet<Event>> optionalAttendeeEvents) {
    // result array list is the list where we did not consider optional people
    // iterate over all TimeRanges in result and calculate the number of attendee overlaps each has
    // find the one with the minimum # of overlaps
    int min = Integer.MAX_VALUE;
    int minIndex = 0;
    HashSet<String> namesToBeRemoved = null;
    for (int i = 0; i < result.size(); i++) {
      TimeRange time = result.get(i);
      // maintain a hashset of optional attendees 
      // this is to avoid double counting certain events that belong to overbooked attendees
      HashSet<String> names = new HashSet<String>();
      for (Event optional : optionalEvents) {
        if (optional.getWhen().overlaps(time)) {
          for (String a : optional.getAttendees()) {
            if (!names.contains(a) && optionalAttendees.contains(a)) {
              names.add(a);
            }
          }
        }
      }
      int count = names.size();
      if (min > count) {
        minIndex = i;
        min = count;
        namesToBeRemoved = names;
      }
    }

    // remove from the map any optional attendees that can definitely make it
    Iterator<Map.Entry<String, HashSet<Event>>> iterate = optionalAttendeeEvents.entrySet().iterator();
    while (iterate.hasNext()) {
      Map.Entry<String, HashSet<Event>> e = iterate.next();
      String key = e.getKey();
      if (!namesToBeRemoved.contains(key)) {
          iterate.remove();
      }
    }
    
    return result.get(minIndex);
  }

  public ArrayList<TimeRange> removeTimeConflicts(ArrayList<TimeRange> allBusyTimeRanges, HashMap<String, HashSet<Event>> optionalAttendeeEvents, TimeRange needToBeFree, long duration) {
    ArrayList<TimeRange> cp = new ArrayList<>(allBusyTimeRanges);
    ArrayList<TimeRange> result = null; 
    // go through each optional person who has a meeting in this time range we are trying to free: 
    // check whether there exist an event that conflicts with the time range
    // that we want to free up. If so remove all their events. Repeat until there is enough space
    for (String entry : optionalAttendeeEvents.keySet()) {
      boolean removeAll = false;
      for(Event e : optionalAttendeeEvents.get(entry)) {
        if (removeAll) {
          cp.remove(e.getWhen());
        } else if (e.getWhen().overlaps(needToBeFree)) {
          cp.remove(e.getWhen());
          removeAll = true;
        }
      }

      // check whether up to this point we have created at least one time gap 
      result = getFreeTime(getIntersection(cp), duration);
      if (result.size() > 0) {
        break;
      }
    }
    return result;
  }

  // method to help with debugging
  public String printOutArrayList(ArrayList<TimeRange> times) {
      StringBuilder sb = new StringBuilder();
      for (TimeRange t : times) {
          sb.append(t.toString() + "\n");
      }
      return sb.toString();
  }

  // method to help with debugging
  public String printOutHashSet(HashSet<String> set) {
      StringBuilder sb = new StringBuilder();
      for (String s : set) {
          sb.append(s + "\n");
      }
      return sb.toString();
  }

  // method to help with debugging
  public String printOutCollection(Collection<String> set) {
      StringBuilder sb = new StringBuilder();
      for (String s : set) {
          sb.append(s + "\n");
      }
      return sb.toString();
  }
}
