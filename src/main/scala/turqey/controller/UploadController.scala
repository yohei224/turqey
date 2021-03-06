package turqey.controller

import org.scalatra._
import org.scalatra.ScalatraServlet
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}
import scalikejdbc._

import turqey.entity._
import turqey.utils._
import turqey.utils.Json
import turqey.utils.Implicits._

class UploadController extends AuthedController with FileUploadSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024)))

  override val path = "upload"

  post("/attach/"){ implicit dbSession =>
    contentType = "application/json"

    val user = turqey.servlet.SessionHolder.user.get

    val result = fileMultiParams("file") map {
      f => {
        val saver = new FileUtil.FileSaver(f)
        val newRec = Upload.create(
          name      = f.name,
          mime      = saver.mimeType,
          isImage   = saver.isImage,
          size      = f.size,
          ownerId   = user.id
        )
        saver.saveUpload(newRec.id.toString)
        newRec
      }
    }
    
    Json.toJson(result)
  }

  getWithoutDB("/attach/:id/") {
    import java.net.URLEncoder
    val user = turqey.servlet.SessionHolder.user.get

    val id = params.get("id").getOrElse(redirectFatal("/")).toLong

    Upload.find(id).map{ rec =>
      contentType = rec.mime
      response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(rec.name, "utf-8"))
      new java.io.File(FileUtil.uploadsDir, id.toString)
    } getOrElse( resourceNotFound() )
  }

}

