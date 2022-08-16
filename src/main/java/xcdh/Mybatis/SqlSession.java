package xcdh.Mybatis;



import xcdh.MVC.annotation.Autowired;
import xcdh.Mybatis.annotation.Param;
import app.Dao.StudentDao;
import xcdh.springboot.anotation.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class SqlSession {


   @Autowired
    Configuration config;


    public Object getProxy(Class<?> clazz) {

        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Map<String, String> params = new HashMap<>();
                String methodName = method.getName();
                String simpleName = method.getGenericReturnType().getClass().getSimpleName();


                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Param param = (Param) parameterAnnotations[i][0];
                    String paramName = param.value();
                    String paramValue = (String) args[i];
                    params.put(paramName, paramValue);                          //Todo
                }
                sqlMapping sq = ParaseXml.parase(clazz.getSimpleName(), params, methodName);
                return excute(simpleName, sq);
            }
        });
    }

    private Object excute(String simpleName, sqlMapping sm) {


        try {

            if (simpleName.equals("ParameterizedTypeImpl")) {
                ResultSet rs = config.getConnection().executeQuery(sm.getSql());

                String resultType = sm.getResultType();
                Class<?> aClass = Class.forName(resultType);


                Field[] fields = aClass.getDeclaredFields();

                List<Object> list = new ArrayList<>();

                while (rs.next()) {
                    Object cast = aClass.cast(aClass.newInstance());

                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);

                        fields[i].set(cast, rs.getString(i + 1));
                        list.add(cast);
                    }
                }


                return list;
            } else {
                Statement connection = config.getConnection();
                connection.execute(sm.getSql());

                return null;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

       return null;
    }



}

