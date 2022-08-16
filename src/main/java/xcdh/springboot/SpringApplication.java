package xcdh.springboot;



import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ResourceSet;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import xcdh.MVC.DispatcherServlet;

import java.io.File;
import java.lang.reflect.InvocationTargetException;


public class SpringApplication {

    public static void run(Class<?> clazz) {
        startTomcat(clazz);

    }

    private static void startTomcat(Class<?> clazz) {
        //启动tomcat
        Tomcat tomcat = new Tomcat();
        //分配端口需要使用Connector对象
        Connector connector = new Connector();
        connector.setPort(8489);

        connector.setURIEncoding("UTF-8");
        tomcat.getService().addConnector(connector);



        Context context = tomcat.addContext("/", null);
        context.setReloadable(false);

        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        Wrapper servlet = Tomcat.addServlet(context, "DispatcherServlet", dispatcherServlet);

       servlet.addMapping("/");
        servlet.addInitParameter("initClass",clazz.getName());

        servlet.setLoadOnStartup(1);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        tomcat.getServer().await();
    }


}

