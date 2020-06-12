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

    //  get a list of individuals we need to consider from request
    Collection<String> attendees = request.getAttendees();
    long duration = request.getDuration();
    if (duration > TimeRange.END_OF_DAY - TimeRange.START_OF_DAY) {
        return new ArrayList<TimeRange>();
    }
    
    // isolate the timeranges from events that pertain to those individuals
    ArrayList<TimeRange> timeRanges = new ArrayList<TimeRange>();
    for (Event e : events) {
        for (String a: e.getAttendees()) {
            if (attendees.contains(a)) {
                timeRanges.add(e.getWhen());
            }
        }
    }

    // find the TimeRanges that work:
    // first sort
    Collections.sort(timeRanges, TimeRange.ORDER_BY_START);
    System.out.println("after sorting: " + toString(timeRanges));
    int i = 0;
    while(i< timeRanges.size()) {
        // we are at the end of the list or there is no overlap between the current event and the next event
        if (i == timeRanges.size()-1 || !timeRanges.get(i).overlaps(timeRanges.get(i+1))) {
            i++;
        } else {
        // there is an overlap so we remove both events and create a TimeRange that is the intersection of the two
            TimeRange curr = timeRanges.remove(i);
            TimeRange next = timeRanges.remove(i);

            int start = Math.min(curr.start(), next.start());
            int end = Math.max(curr.end(), next.end());
            timeRanges.add(i, TimeRange.fromStartEnd(start, end, false));
        }
    }

    // find the free part of the day and weed out time ranges thats too short
    ArrayList<TimeRange> result = new ArrayList<TimeRange>();
    int endTime = TimeRange.START_OF_DAY; // end time of the last event we examined
    
    for (int k = 0; k < timeRanges.size(); k++) {
        // case 1: first event of the day starts at the begining of the day
        if (k ==0 && timeRanges.get(k).start() == TimeRange.START_OF_DAY){
            endTime = timeRanges.get(k).end();
            continue;
        }
        // case 2:the normal case
        int startTime = timeRanges.get(k).start();
        if (startTime - endTime >= duration) {
            result.add(TimeRange.fromStartEnd(endTime, startTime, false));     
        } 
        endTime = timeRanges.get(k).end();
    }

    // case 3: the final interval we need to consider at the end of the day
    if (TimeRange.END_OF_DAY - endTime >= duration) {
        result.add(TimeRange.fromStartEnd(endTime, TimeRange.END_OF_DAY, true));
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
