
package com.fixeight.view


object Util {

  def root (implicit request:javax.servlet.http.HttpServletRequest):String = { request.getContextPath }

}

