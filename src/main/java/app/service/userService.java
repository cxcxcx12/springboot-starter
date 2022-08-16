package app.service;


import app.Dao.StudentDao;
import app.pojo.Student;
import xcdh.MVC.annotation.Autowired;
import xcdh.MVC.annotation.Service;

import java.util.List;

@Service
public class userService {

    @Autowired
    StudentDao studentDao;


      public void insertuser(String name, String age, String id){


          studentDao.insertuser(name, age, id);


      }

    public List<Student> find(String name, String age){


        return studentDao.find(name,age);


    }



}
