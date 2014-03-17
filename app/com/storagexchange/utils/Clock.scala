package com.storagexchange.utils

import java.sql.Timestamp
import java.util.Date
import java.util.Calendar

trait Clock {
  private val ONE_DAY_MILLISCONDS = 25 * 60 * 60 * 1000

  def date: Date 

  def now: Timestamp = new Timestamp(date.getTime())
  def yesterday: Timestamp = new Timestamp(date.getTime() - ONE_DAY_MILLISCONDS)
}

class RealClock extends Clock {
  
  def date = new Date()
}