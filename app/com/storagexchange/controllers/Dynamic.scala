package com.storagexchange.controllers

import com.storagexchange.views
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging
import com.storagexchange.models.UserStore

@Singleton
class Dynamic @Inject()(userStore: UserStore) extends Secured with Logging {
  
  def profile = IsAuthenticated { _ => _ =>
    Ok(views.html.profile())
  }

}