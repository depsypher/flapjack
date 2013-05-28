package flapjack.server;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Run the webapp in an embedded jetty container
 *
 * @author Ray Vanderborght
 */
public class Jetty {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		WebAppContext context = new WebAppContext();

		String webXml = new File("webapp/WEB-INF/web.xml").getCanonicalPath();
		context.setDescriptor(webXml);
		context.setResourceBase("webapp");
		context.setContextPath("/flapjack");
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		server.start();
		server.join();
	}
}
