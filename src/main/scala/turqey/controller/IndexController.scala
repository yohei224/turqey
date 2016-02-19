package turqey.controller

import org.scalatra._
import org.scalatra.scalate.ScalateSupport._
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.servlet._

class IndexController extends ControllerBase {
  override val path = ""
  override val shouldLoggedIn = false
  
  val pagesize = 20

  val entry = get("/") {
    if (!(SessionHolder.user isDefined)){
      redirect(url(login))
    }
    
    val usrId = SessionHolder.user.get.id

    val articleIds = Article.findAllId().grouped(pagesize).toSeq
    val stockIds = ArticleStock.findAllBy(
      sqls.eq(ArticleStock.column.userId, usrId)
    ).map( x => x.articleId ).grouped(pagesize).toSeq
    val ownIds = Article.findAllIdBy(
      sqls.eq(Article.column.ownerId, usrId)
    ).grouped(pagesize).toSeq
    val commentedIds = ArticleComment.findAllBy(
      sqls.eq(ArticleStock.column.userId, usrId)
    ).map( x => x.articleId ).grouped(pagesize).toSeq
    val followingIds = ArticleTagging.followingArticleIds(usrId).grouped(pagesize).toSeq

    jade("/index", 
      "articleIds" -> articleIds,
      "stockIds" -> stockIds,
      "ownIds" -> ownIds,
      "commentedIds" -> commentedIds,
      "followingIds" -> followingIds)
  }
  
  val login = get("/login") {
    jade("/login")
  }

  post("/login") {
    val id   = params.get("loginId")
    val pass = params.get("password")

    val digestedPass = turqey.utils.Digest.get(pass.get)

    val usr = User.findBy(sqls.eq(User.u.loginId, id).and.eq(User.u.password, digestedPass))
    usr  match {
      case Some(user: User) => {
        session("user") = new UserSession(user.id, user.name, user.imgUrl, user.root)

        user.copy(
          lastLogin = Some(new org.joda.time.DateTime())
        ).save()
        
        redirect(fullUrl("/", includeServletPath = false) + "/")
      }
      case None => jade("/login")
    }
    
  }

  val logout = get("/logout") {
    session.invalidate()
    redirect(url(login))
  }

}
