package io.renren.common.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 @Description: xml解析
 @Author: ESION.CT
 @Date: 2023/5/20 14:19
 */
public class XmlUtil {

  /** 标签属性 */
  private static final String TAG_ATTR = "attr";

  /** 创建的map类型 */
  private static XmlSort xmlSort = XmlSort.NO_SORT;

  /**
   * map to xml
   *
   * @param map map对象
   * @return xml 字符串
   */
  public String mapToXml(Map<String, Object> map) {
    if (map.size() != 1) {
      throw new RuntimeException("map根节点长度不为1");
    }
    String key = "";
    for (String str : map.keySet()) {
      key = str;
    }
    //  创建根节点
    Element rootElement = new DefaultElement(key);
    Document document = new DefaultDocument(rootElement);
    Element node = document.getRootElement();
    Object obj = map.get(key);
    // 断言
    Assert.isAssignable(Map.class, obj.getClass());
    mapNodes(node, (Map<String, Object>) obj);
    return document.asXML();
  }

  /**
   * 父类节点已经创建， map 包含父类
   *
   * @param node node
   * @param map map
   */
  public void mapNodes(Element node, Map<String, Object> map) {
    map.forEach(
        (k, v) -> {
          Object obj = map.get(k);
          // 给当前父类添加属性
          if (TAG_ATTR.equals(k)) {
            Assert.isAssignable(Map.class, obj.getClass());
            Map<String, Object> tagMap = (Map<String, Object>) obj;
            tagMap.forEach(
                (tagKey, tagValue) -> {
                  node.addAttribute(tagKey, (String) tagValue);
                });
            return;
          }
          if (obj instanceof Map) {
            Element newElement = node.addElement(k);
            // map 处理
            Map<String, Object> childMap = (Map<String, Object>) obj;
            mapNodes(newElement, childMap);
          } else if (obj instanceof String) {
            Element newElement = node.addElement(k);
            newElement.setText((String) v);
          } else if (obj instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
            list.forEach(
                itemMap -> {
                  Element newElement = node.addElement(k);
                  mapNodes(newElement, itemMap);
                });
          }
        });
  }

  /**
   * 读取xml文件，返回json字符串
   *
   * @param fileName 文件路径
   * @return json字符串
   * @throws DocumentException 异常
   */
  public String xmlToJson(String fileName) throws DocumentException {
    Map<String, Object> xmlMap = xmlToMap(fileName);
    return JSONUtil.toJsonStr(xmlMap);
  }

  /**
   * 读取xml文件，返回map对象
   *
   * @param fileName 文件路径
   * @return map对象
   * @throws DocumentException 异常
   */
  public static Map<String, Object> xmlToMap(String fileName) throws DocumentException {
    // 创建saxReader对象
    SAXReader reader = new SAXReader();
    // 通过read方法读取一个文件 转换成Document对象
    Document document = reader.read(new File(fileName));
    // 获取根节点元素对象
    Element node = document.getRootElement();
    // 遍历所有的元素节点
    Map<String, Object> map = getNewMap();
    // 处理节点
    listNodes(node, map);
    return map;
  }

  /**
   * 遍历当前节点元素下面的所有(元素的)子节点
   *
   * @param node node
   */
  public static void listNodes(Element node, Map<String, Object> map) {
    Map<String, Object> xiaoMap = getNewMap();
    String nodeKey = node.getName();
    // 获取当前节点的所有属性节点
    List<Attribute> list = node.attributes();
    // 遍历属性节点
    Map<String, Object> attrMap = getNewMap();
    for (Attribute attr : list) {
      attrMap.put(attr.getName(), attr.getValue());
    }
    if (ObjectUtil.isNotEmpty(attrMap)) {
      xiaoMap.put(TAG_ATTR, attrMap);
    }

    // 当前节点下面子节点迭代器
    Iterator<Element> it = node.elementIterator();

    if (!("".equals(node.getTextTrim())) || !it.hasNext()) {
      if("Param".equals(nodeKey)){
        //创建list
        Map<String,List<String>> mapList = new HashMap<>();
        LinkedList<String> lists = new LinkedList<>();
        for (Attribute attr : list) {
          lists.add(attr.getValue());
        }
        mapList.put("Param",lists);
      }else{
        map.put(nodeKey, node.getTextTrim());
      }
    } else {
      // 不为空
      if (ObjectUtil.isEmpty(map.get(nodeKey))) {
        map.put(nodeKey, xiaoMap);
      } else {
        List<Map<String, Object>> childList = null;
        // 获取原来的
        Object obj = map.get(nodeKey);
        if (obj instanceof Iterable) {
          // 非第一个
          childList = (List<Map<String, Object>>) obj;
          childList.add(xiaoMap);
        } else if (obj instanceof Map) {
          // 第一个
          Map<String, Object> childMap = (Map<String, Object>) obj;
          childList = new ArrayList<>();
          childList.add(childMap);
          childList.add(xiaoMap);
        }
        // 添加新的
        map.put(nodeKey, childList);
      }
    }

    // 遍历
    while (it.hasNext()) {
      // 获取某个子节点对象
      Element e = it.next();
      // 对子节点进行遍历
      listNodes(e, xiaoMap);
    }
  }

  /**
   * 获取一个新的map对象
   *
   * @return map对象
   */
  private static Map<String, Object> getNewMap() {
    Object obj = null;
    try {
      obj = xmlSort.getMapClass().newInstance();
      if (obj instanceof Map) {
        return (Map<String, Object>) obj;
      }
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 设置是否排序
   *
   * @param xmlSort 是否排序对象
   */
  public void setXmlSort(XmlSort xmlSort) {
    this.xmlSort = xmlSort;
  }


  public static void main(String[] args) throws DocumentException {
    LinkedHashMap<String, String> hashmap = new LinkedHashMap<>();
    hashmap.put("DAN_TA","铁塔健康监测单塔数据协议");
    hashmap.put("SHUANG_TA","铁塔健康监测双塔数据协议");

    XmlUtil xmlUtil = new XmlUtil();
    String path = xmlUtil.getClass().getClassLoader().getResource("file/data.xml").getPath();
    try {
      String packagePath = path.replaceAll("%20","");
      //解决路径包含中文的情况
      path = java.net.URLDecoder.decode(packagePath,"utf-8");
      System.out.println(path);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    Map<String, Object> map = xmlUtil.xmlToMap(path);
    Map<String, Object> towerMonitor = (HashMap)map.get("TowerMonitor");
    Map<String, Object>  station = (Map)towerMonitor.get("Station");
    //台站信息
    String stationInfo = (String) station.get("StationInfo");
    if(Objects.nonNull(stationInfo)){
      String[] split = stationInfo.split("\\|");
      //台站id
      String id = split[0];
      String name = split[1];
    }
    List<Map<String, Object>> towers = (List)station.get("Tower");
    towers.stream().forEach(it->{
      //根据每个信号源进行组装
      String towerInfo = (String)it.get("TowerInfo");
      if (towerInfo.contains("2#单频天线塔") ||
         towerInfo.contains("1#双频天线塔") ||
         towerInfo.contains("100m自立塔") ||
         towerInfo.contains("76m拉线塔") ||
         towerInfo.contains("单频塔") ||
         towerInfo.contains("双频塔")
      ) {
        Map<String, Object>  second = (Map)it.get("Second");
        //开始解析
        if(Objects.nonNull(second)){
          String param = (String) second.get("Param");
          if(Objects.nonNull(param)){
            if(param.contains("垂直度")||
              param.contains("应力") ||
              param.contains("拉力") ||
              param.contains("风速") ||
              param.contains("风险值")
            ){
              String[] splitArr = param.split("\\|");
              List<String> collect = Arrays.stream(splitArr).collect(Collectors.toList());
               if(!collect.isEmpty()){
                 //日期
                 String date = collect.get(5);
                 //平均值
                 String avg = collect.get(8);

               }
            }

          }
        }
      }

    });
    System.out.println("读取完毕，开始写入...");
    //TODO mysql 写入
  }
}