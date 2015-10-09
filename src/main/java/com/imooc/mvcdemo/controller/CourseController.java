package com.imooc.mvcdemo.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.imooc.mvcdemo.model.Course;
import com.imooc.mvcdemo.service.CourseService;

//声明成controller 会被dispatcher管理 并被spring进行依赖注入？
@Controller
@RequestMapping("/courses")
// /courses/**  courses根下的路径都会被该类拦截
public class CourseController {
	
	private static Logger log = LoggerFactory.getLogger(CourseController.class);

	private CourseService courseService;

	@Autowired
	public void setCourseService(CourseService courseService) {
		this.courseService = courseService;
	}

//	方法级别的mapping
	//本方法将处理 /courses/view?courseId=123 形式的URL   仅处理get方法的请求
//	测试时，浏览器输入"http://localhost:8080/imoocSpringMvc/courses/view?courseId=123"
	@RequestMapping(value="/view", method=RequestMethod.GET)
	public String viewCourse(@RequestParam("courseId") Integer courseId,
			Model model) {
		
		log.debug("In viewCourse, courseId = {}", courseId);
		Course course = courseService.getCoursebyId(courseId);
		model.addAttribute(course);
		return "course_overview";//返回course_overview.jsp页面
	}
	
//	restful风格     路径变量
	//本方法将处理 /courses/view2/123 形式的URL
	@RequestMapping("/view2/{courseId}")
	public String viewCourse2(@PathVariable("courseId") Integer courseId,
			Map<String, Object> model) {
//		这里为毛要定义成Map ？？？
		log.debug("In viewCourse2, courseId = {}", courseId);
		Course course = courseService.getCoursebyId(courseId);
		model.put("course",course);
		return "course_overview";
	}

//	使用servlet  api 
	//本方法将处理 /courses/view3?courseId=123 形式的URL
	@RequestMapping("/view3")
	public String viewCourse3(HttpServletRequest request) {
		
		Integer courseId = Integer.valueOf(request.getParameter("courseId"));		
		Course course = courseService.getCoursebyId(courseId);
		request.setAttribute("course",course);
		
		return "course_overview";
	}
	
//	添加课程
	@RequestMapping(value="/admin", method = RequestMethod.GET, params = "add")
	public String createCourse(){
		return "course_admin/edit";
	}
	
//	保存课程
	@RequestMapping(value="/save", method = RequestMethod.POST)
	public String  doSave(@ModelAttribute Course course){		
		
		log.debug("Info of Course:");
//		利用appache一个类,打印一个对象
		log.debug(ReflectionToStringBuilder.toString(course));
		
		//在此进行业务操作，比如数据库持久化
		course.setCourseId(123);
//		重定向
		return "redirect:view2/"+course.getCourseId();
	}
	
//	上传页面
	@RequestMapping(value="/upload", method=RequestMethod.GET)
	public String showUploadPage(@RequestParam(value= "multi", required = false) Boolean multi){	
		if(multi != null && multi){
			return "course_admin/multifile";	
		}
		return "course_admin/file";		
	}
	
//	上传动作
	@RequestMapping(value="/doUpload", method=RequestMethod.POST)
	public String doUploadFile(@RequestParam("file") MultipartFile file) throws IOException{
		
		if(!file.isEmpty()){
			log.debug("Process file: {}", file.getOriginalFilename());
//			保存,将流写入硬盘文件中
			FileUtils.copyInputStreamToFile(file.getInputStream(), new File("c:\\temp\\imooc\\", System.currentTimeMillis()+ file.getOriginalFilename()));
		}
		
		return "success";
	}
	
	@RequestMapping(value="/doUpload2", method=RequestMethod.POST)
	public String doUploadFile2(MultipartHttpServletRequest multiRequest) throws IOException{
		
		Iterator<String> filesNames = multiRequest.getFileNames();
		while(filesNames.hasNext()){
			String fileName =filesNames.next();
			MultipartFile file =  multiRequest.getFile(fileName);
			if(!file.isEmpty()){
				log.debug("Process file: {}", file.getOriginalFilename());
//				保存,将流写入硬盘文件中
				FileUtils.copyInputStreamToFile(file.getInputStream(), new File("c:\\temp\\imooc\\", System.currentTimeMillis()+ file.getOriginalFilename()));
			}
			
		}
		
		return "success";
	}
	
//	服务器响应请求，返回json格式字符串   
	@RequestMapping(value="/{courseId}",method=RequestMethod.GET)
	public @ResponseBody Course getCourseInJson(@PathVariable Integer courseId){
		return  courseService.getCoursebyId(courseId);
	}
	
	
//	浏览器输入jsontype/123
	@RequestMapping(value="/jsontype/{courseId}",method=RequestMethod.GET)
	public ResponseEntity<Course> getCourseInJson2(@PathVariable Integer courseId){
		Course course =   courseService.getCoursebyId(courseId);		
		return new ResponseEntity<Course>(course, HttpStatus.OK);
	}
	
}
