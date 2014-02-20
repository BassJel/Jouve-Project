// Copyright 2006 Google Inc.  All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.doculibre.constellio.entities.scheduler;

import java.io.Serializable;

import com.google.enterprise.connector.scheduler.ScheduleTime;
import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;

/**
 * An interval of time used for schedules.
 */
@SuppressWarnings("serial")
public class SerializableScheduleTimeInterval implements Serializable, Cloneable {
  private SerializableScheduleTime startTime;
  private SerializableScheduleTime endTime;

  public SerializableScheduleTimeInterval(ScheduleTimeInterval timeInterval) {
      this(new SerializableScheduleTime(timeInterval.getStartTime()), new SerializableScheduleTime(
              timeInterval.getEndTime()));
  }

  public SerializableScheduleTimeInterval(SerializableScheduleTime startTime, SerializableScheduleTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public SerializableScheduleTime getEndTime() {
    return endTime;
  }

  public void setEndTime(SerializableScheduleTime endTime) {
    this.endTime = endTime;
  }

  public SerializableScheduleTime getStartTime() {
    return startTime;
  }

  public void setStartTime(SerializableScheduleTime startTime) {
    this.startTime = startTime;
  }

  public ScheduleTimeInterval toTimeInterval() {
    ScheduleTime notSerializableStartTime = startTime.toScheduleTime();
    ScheduleTime notSerializableEndTime = endTime.toScheduleTime();  
    ScheduleTimeInterval timeIterval = new ScheduleTimeInterval(notSerializableStartTime, notSerializableEndTime);
    return timeIterval;
  }

  @Override
  public SerializableScheduleTimeInterval clone() {
    return new SerializableScheduleTimeInterval(this.toTimeInterval());  
  }
  
}
