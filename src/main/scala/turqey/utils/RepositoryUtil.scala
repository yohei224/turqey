package turqey.utils

import glitch._
import glitch.GitRepository._
import java.io.File

class ReadOnlyArticleRepository(id: Long) extends ArticleRepository(id)

class WritableArticleRepository(id: Long, ident: Ident) extends ArticleRepository(id) {
  
  def withLock[T](f: => T) = LockByVal.withLock(repo.getDirectory.toString)(f)
  def existsOr(b: GitRepository#Branch)( f: => GitRepository#Branch ): GitRepository#Branch = if(b.exists) b else f

  override val master = withLock {
    new MasterBranch(existsOr(repo.branch("master"))( repo.initialize("initial commit", ident).branch("master") ))
  }
  
  def draftName = "draftOf" + ident.userId.toString
  val draft = new DraftBranch( existsOr(repo.branch(draftName))( master.createNewBranch(draftName)), master )
  
  sealed abstract class WritableBranch(branch: GitRepository#Branch) extends Branch(branch) {
    def commit(article: ArticleWhole): GitRepository#Commit = branch.commit(
      article.constructDir(), 
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format new java.util.Date(),
      ident
    )
    def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]): GitRepository#Commit
  }
  
  class MasterBranch(branch: GitRepository#Branch) extends WritableBranch(branch) {
    
    override def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) = withLock {
      commit(ArticleWhole(id, title, content, tagIds, attachments))

      this.mergeTo(draft, ident)
      draft.mergeTo(master, ident, true)

      val head = master.head
      head.addTag(
        "%tY%<tm%<td_%<tH%<tM%<tS" format new java.util.Date(),
        "%tY%<tm%<td_%<tH%<tM%<tS" format new java.util.Date(),
        ident)
      head
    }
  }

  class DraftBranch(branch: GitRepository#Branch, master: MasterBranch) extends WritableBranch(branch) {
    def isBehindMaster: Boolean = this.isBehind(master)
    override def save(title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) = withLock {
      commit(ArticleWhole(id, title, content, tagIds, attachments))
      draft.head
    }
    def isMergableFromMaster: Boolean = master.isMergableTo(this)
    def isMergableToMaster: Boolean = this.isMergableTo(master)

    def mergeFromMaster: Unit = master.mergeTo(this, ident)
    def mergeToMaster: Unit = this.mergeTo(master, ident)
  }
  
}


sealed abstract class ArticleRepository(id: Long) {
  import scala.language.implicitConversions
  
  val repo: GitRepository = GitRepository.getInstance(new File(FileUtil.articleBaseDir, id.toString + ".git"))
  
  val master: Branch = new Branch(repo.branch("master"))
  
  sealed class Branch(branch: GitRepository#Branch) extends repo.Branch(branch.name) {
    def articleAt(commitId: String): ArticleWhole = articleAt(new repo.Commit(commitId))
    def articleAt(commit: GitRepository#Commit): ArticleWhole = {
      val dir = commit.getDir
      val content = new String(dir.file(ArticleWhole.ARTICLE_MD).bytes)
      val attrs   = Json.parseAs[ArticleAttrs](new String(dir.file(ArticleWhole.ARTICLE_ATTRS).bytes))
  
      ArticleWhole(id, attrs.title, content, attrs.tagIds, attrs.attachments)
    }
    def headArticle: ArticleWhole = articleAt(head)
  }

  def listTags(id: Long): Seq[GitRepository#Tag] = {
    import collection.JavaConversions._
    repo.listTags()
  }
}

case class Attachment(id: Long, name: String, isImage: Boolean, mime: String, size: Long)
case class ArticleAttrs(title: String, tagIds: Seq[Long], attachments: Seq[Attachment])

case class ArticleWhole(id: Long, title: String, content: String, tagIds: Seq[Long], attachments: Seq[Attachment]) {
  def attrs = ArticleAttrs(this.title, this.tagIds, this.attachments)
  def constructDir() = new GitRepository.Dir()
    .put(ArticleWhole.ARTICLE_MD,    this.content.getBytes())
    .put(ArticleWhole.ARTICLE_ATTRS, Json.toJson(this.attrs).getBytes())
}
object ArticleWhole {
  val ARTICLE_MD    = "Article.md"
  val ARTICLE_ATTRS = "ArticleAttrs.json"
}

case class Ident(userId: Long, name: String, email: String) extends GitRepository.Ident(name, email)

class ConflictException extends Exception

