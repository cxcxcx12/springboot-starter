package xcdh.MVC;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.Server;
import org.apache.catalina.servlets.DefaultServlet;
import xcdh.MVC.annotation.Autowired;
import xcdh.MVC.annotation.RequestBody;
import xcdh.MVC.annotation.RequestMapping;
import xcdh.MVC.annotation.RequestParam;


import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


public class DispatcherServlet extends HttpServlet {
    List<Handlerr> handlers;

    @Override
    public void init(ServletConfig config) throws ServletException {

        String packageUrl = config.getInitParameter("initClass");
        WebApplicationContext wc = new WebApplicationContext(packageUrl);
        wc.refresh();
       handlers = wc.getHandlers();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

       dipatchRequet(req,resp);
    }

    private void dipatchRequet(HttpServletRequest req, HttpServletResponse resp) {

        String s = req.getRequestURI();
        Map<String,String[]> parameterMap = req.getParameterMap();

        for (Handlerr handler : handlers) {
            if (handler.getUrl().equals(s)) {

                Object controller = handler.getTarget();


                Method method = handler.getMethod();
                Class<?>[] parameterTypes = method.getParameterTypes();  //只能拿到修饰符
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                Object[] params=new Object[parameterAnnotations.length];
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    if (parameterAnnotations[i][0]!=null) {
                        if ((parameterAnnotations[i][0].annotationType().equals(RequestParam.class))){
                            RequestParam requestParam = (RequestParam) parameterAnnotations[i][0];
                            String value = requestParam.value();
                            String[] valuee = parameterMap.get(value);
                            String s1 = valuee[0];
                            params[i] =parameterTypes[i].cast(s1);   //cast 就是强制转换！！ 有待商榷
                            //注意这里可能会根据参数类型 进行强制装换！！

                        }
                        else if ((parameterAnnotations[i][0].annotationType().equals(RequestBody.class))){
                            try {
                                JSONObject json = JsonUtils.getJsonReq(req);
                                Object o = parameterTypes[i].newInstance();
                                Field[] fields = parameterTypes[i].getDeclaredFields();
                                for (Field field : fields) {
                                    field.setAccessible(true);
                                    if (json.get(field.getName()) != null) {
                                        String filed_value = String.valueOf(json.get(field.getName()));
                                         field.set(o, filed_value);
                                    }

                                }
                                params[i]=o;
                            } catch (Exception e) {e.printStackTrace();}
                        }

                    }
                    //我这里默认是 一个参数用param ，二个及以上的用requestbody。  所有剩下的只会有httpservlet
                    else{
                        if (parameterTypes[i].equals(HttpServletRequest.class)) {
                            params[i]=req;
                        }
                        else if (parameterTypes[i].equals(HttpServletResponse.class)){
                            params[i]=resp;
                        }
                    }

                }
                try {

                    Object result = method.invoke(controller, params);
                    if(method.getReturnType().equals(String.class) ){
                        //跳转JSP
                        String viewName=(String)result;
                        // forward:/success.jsp
                        if(viewName.contains(":")){
                            String viewType=viewName.split(":")[0];
                            String viewPage=viewName.split(":")[1];
                            if(viewType.equals("forward")){
                                req.getRequestDispatcher(viewPage).forward(req,resp);
                            }else{
                                // redirect:/user.jsp
                                resp.sendRedirect(viewPage);
                            }
                        }else{
                            //默认就转发
                            req.getRequestDispatcher(viewName).forward(req,resp);
                        }
                    }else{

                            //将返回值转换成 json格式数据
                            ObjectMapper objectMapper = new ObjectMapper();
                            String json = objectMapper.writeValueAsString(result);
                            resp.setContentType("text/html;charset=utf-8");
                            PrintWriter writer = resp.getWriter();
                            writer.print(json);
                            writer.flush();
                            writer.close();


                    }
                } catch (Exception e) {
                    e.printStackTrace();}



            }
        }


    }


}
