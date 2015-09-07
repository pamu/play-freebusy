package global

import models.DB
import play.api.{Application, GlobalSettings}

/**
 * Created by pnagarjuna on 07/09/15.
 */
object AppGlobal extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    DB.init.value
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)
  }
}
