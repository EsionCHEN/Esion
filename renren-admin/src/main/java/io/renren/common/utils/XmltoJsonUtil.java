package io.renren.common.utils;
/*
 *文件名: XmltoJsonUtil
 *创建者: Liu Yong Kang
 *创建时间:2023/1/12 10:43
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;

public class XmltoJsonUtil{
        /**
         * 将xml转换为json对象
         */
        public static JSONObject xmlToJson(String xml) throws DocumentException {
            JSONObject jsonObject = new JSONObject();
            try {

                Document document = DocumentHelper.parseText(xml);
                //获取根节点元素对象
                Element root = document.getRootElement();
                iterateNodes(root, jsonObject);
                return jsonObject;
            }catch (Exception e){
                return jsonObject;
            }

        }

        /**
         * 遍历元素
         * @param node
         * @param json
         */
        private static void iterateNodes(Element node, JSONObject json) {
            //获取当前元素名称
            String nodeName = node.getName();
            //判断已遍历的JSON中是否已经有了该元素的名称
            if(json.containsKey(nodeName)){
                //该元素在同级下有多个
                Object Object = json.get(nodeName);
                JSONArray array = null;
                if(Object instanceof JSONArray){
                    array = (JSONArray) Object;
                }else {
                    array = new JSONArray();
                    array.add(Object);
                }
                //获取该元素下所有子元素
                List<Element> listElement = node.elements();
                if(listElement.isEmpty()){
                    //该元素无子元素，获取元素的值
                    String nodeValue = node.getTextTrim();
                    array.add(nodeValue);
                    json.put(nodeName, array);
                    return ;
                }
                //有子元素
                JSONObject newJson = new JSONObject();
                //遍历所有子元素
                for(Element e:listElement){
                    //递归
                    iterateNodes(e,newJson);
                }
                array.add(newJson);
                json.put(nodeName, array);
                return ;
            }
            //该元素同级下第一次遍历
            //获取该元素下所有子元素
            List<Element> listElement = node.elements();
            if(listElement.isEmpty()){
                //该元素无子元素，获取元素的值
                String nodeValue = node.getTextTrim();
                json.put(nodeName, nodeValue);
                return ;
            }
            //有子节点，新建一个JSONObject来存储该节点下子节点的值
            JSONObject object = new JSONObject();
            for(Element e:listElement){
                //递归
                iterateNodes(e,object);
            }
            json.put(nodeName, object);
            return ;
        }
}
