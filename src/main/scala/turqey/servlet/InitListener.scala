package turqey.servlet

import javax.servlet._
import akka.actor.{Props, ActorSystem}

import scalikejdbc._
import scalikejdbc.config._

import org.flywaydb.core._

import turqey.entity.User
import turqey.utils._
import turqey.actor._

class InitListener extends ServletContextListener {

  override def contextInitialized(event: ServletContextEvent):Unit = {
    DBs.setupAll()

    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool.dataSource())
    flyway.migrate()
    
    if (User.countBy(sqls.eq(User.u.loginId, "root")) == 0){
      User.create(
        email    = "root",
        loginId  = "root",
        name     = "root",
        password = Digest.get("root"),
        imgUrl   = "",
        root     = true
      )
    }
    
    ServletContextHolder.init(event.getServletContext)

    import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
    import akka._
    
    val system = ActorSystem("SampleSystem")
    val scheduler = QuartzSchedulerExtension(system)

  }

  override def contextDestroyed(event: ServletContextEvent):Unit = {
    DBs.closeAll()
  }

}

case class ServletContextHolder(srvCtx: ServletContext)
  
object ServletContextHolder {
  var srvCxt:ServletContext = null

  def init(instance: ServletContext): Unit = { this.srvCxt = instance }

  def get:ServletContext = { this.srvCxt } 
  def root:String = { this.srvCxt.getContextPath }
  def assets:String = { this.root + "/assets" }
}


