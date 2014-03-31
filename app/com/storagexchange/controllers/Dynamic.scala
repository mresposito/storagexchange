package com.storagexchange.controllers

import com.storagexchange.views
import javax.inject.Singleton
import javax.inject.Inject
import com.typesafe.scalalogging.slf4j.Logging
import com.storagexchange.models.UserStore

@Singleton
class Dynamic @Inject()(userStore: UserStore) extends Secured with Logging {
  
  def profile = IsAuthenticated { username => _ =>
    userStore.getByEmail(username).map { user =>
	    Ok(views.html.dynamic.profile(user))
    }.getOrElse {
      Redirect(routes.Application.signup)
    }
  }
}