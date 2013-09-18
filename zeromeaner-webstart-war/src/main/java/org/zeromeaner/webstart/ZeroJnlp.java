package org.zeromeaner.webstart;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet implementation class ZeroJnlp
 */
public class ZeroJnlp extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ZeroJnlp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jnlp = IOUtils.toString(ZeroJnlp.class.getResource("zero.jnlp"));
		String url = request.getRequestURL().toString();
		String codebase = url.replaceAll("/[^/]*$", "");
		jnlp = jnlp.replaceAll("\\$\\{codebase\\}", codebase);
		
		response.setDateHeader("Last-Modified", System.currentTimeMillis());
		// IE won't download JNLP file if Cache-Control header presents
		//response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Expires", "Mon, 26 Jul 1990 05:00:00 GMT");
		
		IOUtils.write(jnlp, response.getWriter());
	}

}
