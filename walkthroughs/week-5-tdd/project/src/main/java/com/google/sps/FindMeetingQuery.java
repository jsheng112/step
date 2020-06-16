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
    Collection<String> allAttendees = new ArrayList<String>();
    for (String person : attendees) {
        allAttendees.add(person);
    }
    for (String person : request.getOptionalAttendees()) {
        allAttendees.add(person);
    }

    // isolate the timeranges from events that pertain to required and optional attendees
    ArrayList<TimeRange> busyTimeRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> allBusyTimeRanges = new ArrayList<TimeRange>();
    for (Event e : events) {
      for (String a: e.getAttendees()) {
        if (attendees.contains(a)) {
          busyTimeRanges.add(e.getWhen());
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
    allBusyTimeRanges = getIntersection(allBusyTimeRanges);

    ArrayList<TimeRange> result;
    // if the intersection of all events for both mandatory and optional attendees take up the
    // whole day then just get the free times for the mandatory people
    if (allBusyTimeRanges.size() == 1 && allBusyTimeRanges.get(0).equals(TimeRange.WHOLE_DAY)) {
      result = getFreeTime(busyTimeRanges, duration);
    } else {
      result = getFreeTime(allBusyTimeRanges, duration);
      if (result.size() == 0) {
        result = getFreeTime(busyTimeRanges, duration);
      }
    }
    return result;
  }

  /* find the intersection of all the non-available time ranges */
  public ArrayList<TimeRange> getIntersection(ArrayList<TimeRange> busyTimeRanges) {
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


  // method to help with debugging
  public String printOutArrayList(ArrayList<TimeRange> times) {
      StringBuilder sb = new StringBuilder();
      for (TimeRange t : times) {
          sb.append(t.toString() + "\n");
      }
      return sb.toString();
  }
}
