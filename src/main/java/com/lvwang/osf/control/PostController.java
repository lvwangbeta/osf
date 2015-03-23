package com.lvwang.osf.control;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.lvwang.osf.model.Post;
import com.lvwang.osf.model.Tag;
import com.lvwang.osf.model.User;
import com.lvwang.osf.service.EventService;
import com.lvwang.osf.service.FeedService;
import com.lvwang.osf.service.PostService;
import com.lvwang.osf.service.RelationService;
import com.lvwang.osf.service.TagService;
import com.lvwang.osf.util.Dic;
import com.lvwang.osf.util.Property;

@Controller
@RequestMapping("/post")
public class PostController {
	
	@Autowired
	@Qualifier("postService")
	private PostService postService;
	
	@Autowired
	@Qualifier("relationService")
	private RelationService relationService;
	
	@Autowired
	@Qualifier("tagService")
	private TagService tagService;
	
	@Autowired
	@Qualifier("eventService")
	private EventService eventService;
	
	@Autowired
	@Qualifier("feedService")
	private FeedService feedService;
	
	@RequestMapping("/{id}")
	public ModelAndView post(@PathVariable("id") int id) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("post", postService.findPostByID(id));
		mav.setViewName("post/index");
		return mav;
	}
	
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String createPost() {
		return "post/create";
	}
	
	@ResponseBody
	@RequestMapping(value="/create", method=RequestMethod.POST)
	public Map<String, Object> createPost(					
						@RequestParam("title") String title,
						@RequestParam("content") String content,
						@RequestParam("post_status") int post_status,
						@RequestParam("comment_status") int comment_status,
						@RequestParam("tags") String param_tags,
						HttpSession session) {
				
		User user = (User)session.getAttribute("user");
		//1 save post
		Map<String, Object> map = postService.newPost(user.getId(), 
													  title, 
													  content, 
													  post_status, 
													  comment_status,
													  param_tags);
		String status = (String)map.get("status");
		Post post = (Post)map.get("post");
		
		//2 add event 
		if(Property.SUCCESS_POST_CREATE.equals(status)) {
			int event_id = eventService.newEvent(Dic.OBJECT_TYPE_POST, post);
			
			//3 push to followers
			if(event_id !=0 ) {
				feedService.push(user.getId(), event_id);
			}			
		}
		return map;
		
	}
	
	
	
	
}