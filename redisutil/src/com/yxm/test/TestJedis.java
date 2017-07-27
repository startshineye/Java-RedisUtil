package com.yxm.test;
import java.util.List;
import com.yxm.bean.Message;
import com.yxm.utils.RedisUtil;
public class TestJedis {
  public static void main(String[] args) throws Exception {
	 //连接上jedis
	  
	  Message message = new Message();
	  message.setId("1");
	  message.setContent("中国");
	  
	  Message message1 = new Message();
	  message1.setId("2");
	  message1.setContent("俄罗斯");
	  //放入String,list,bean
	 /* List<String> list = new ArrayList<String>();
	  ArrayList<Message> objList = new ArrayList<Message>();
	  list.add("a");
	  list.add("b");
	  list.add("c");
	  
	  
	  objList.add(message);
	  objList.add(message1);
	  
	  RedisUtil.setString("1101","zs");
	  RedisUtil.setList("1101::list",list,true);
	  RedisUtil.setObject("1101::msg1",message);
	  RedisUtil.setList("1101::msg",objList);
	  
	 //获取String,list,bean
	  String oStr = RedisUtil.getString("1101");
	  List<String> oList = RedisUtil.getList("1101::list", 0, -1);
	  Message object = RedisUtil.getObject("1101::msg1", Message.class);
	  List<Message> list2 = RedisUtil.getList("1101::msg");*/
	  
	  //再次添加
	 Message msg3 =  new Message();
	 msg3.setId("3");
	 msg3.setContent("美国");
	 RedisUtil.addObjToList("1101::msg", msg3);
	 RedisUtil.addObjToList("1101::msg", message1);
	 RedisUtil.addObjToList("1101::msg", message);
	 
	 List<Message> list2 = RedisUtil.getAllObjFromList("1101::msg");
	  //输出
	 /* System.out.println("oStr["+oStr+"] oList["+oList.toString()+"] object["+object.toString()+"] objList["+list2.toString()+"]");*/
  }
}
