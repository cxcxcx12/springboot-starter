package app.controller;


import app.service.userService;
import xcdh.MVC.annotation.Autowired;
import xcdh.MVC.annotation.Controller;
import xcdh.MVC.annotation.RequestMapping;
import xcdh.MVC.annotation.RequestParam;

@Controller
public class userController {


    @Autowired
    userService service;

    @RequestMapping("/user")
    public Object getuser(@RequestParam("name")String name, @RequestParam("age")String age, @RequestParam("id")String id){



        service.insertuser(name,age,id);

        return "index.jsp";
    }

    @RequestMapping("/find")
    public Object getuser(@RequestParam("name")String name, @RequestParam("age")String age){


       return service.find(name,age);


    }



}
