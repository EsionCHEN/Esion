package io.renren.common.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import javax.xml.parsers.*;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class XmlToMap {
    public static Map<String, Object> xmlToMap(Element element) {
        Map<String, Object> map = new HashMap<String, Object>();
        NodeList nodeList = element.getChildNodes();
        if (nodeList.getLength() == 0 && element.getAttributes().getLength() == 0) {
            map.put(element.getTagName(), element.getTextContent());
            return map;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Map<String, Object> childMap = xmlToMap((Element) node);
                Object child = map.get(node.getNodeName());
                if (child == null) {
                    map.put(node.getNodeName(), childMap);
                } else if (child instanceof List) {
                    ((List) child).add(childMap);
                } else {
                    List<Object> list = new ArrayList<Object>();
                    list.add(child);
                    list.add(childMap);
                    map.put(node.getNodeName(), list);
                }
            } else if (node instanceof Attr) {
                map.put(node.getNodeName(), node.getNodeValue());
            }else{
                String textContent = node.getTextContent();
                if(textContent.contains("|")){
                    map.put(String.valueOf(i),textContent);
                }
            }
        }
        return map;
    }


    public static String readFileToString(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static List<String> jsonToSplit(String json){
        List<String> strList = Lists.newArrayList();
        String[] split = json.split("<TowerMonitor>");
        for (String s : Arrays.asList(split)) {
            StringBuffer sb = new StringBuffer();
            if(StringUtils.isNotBlank(s.trim())){
                sb.append("<TowerMonitor>");
                sb.append(s);
                String fomartStr = sb.toString();
                if (fomartStr.contains("<MD5>")) {
                    //<MD5>EA2E7503581CD7D6A6A7E19572657F00</MD5> 固有长度43
                    fomartStr = fomartStr.substring(0,fomartStr.length()-44);
                }
                strList.add(fomartStr);
            }
        }
        return strList;
    }

    /**
     * 16进制Str转byte[]
     *
     * @param hexStr
     * @return
     */
    public static byte[] HexStrToByteArray(String hexStr) {
        if (hexStr == null) {
            return null;
        }
        if (hexStr.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = hexStr.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * byte[]转16进制Str
     *
     * @param byteArray
     */
    public static String ByteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            int temp = byteArray[i] & 0xFF;
            hexChars[i * 2] = hexArray[temp >>> 4];
            hexChars[i * 2 + 1] = hexArray[temp & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 16进制的Str转Str
     *
     * @param hexStr
     * @return
     */
    public static String HexStrToStr(String hexStr) {
        //能被16整除,肯定可以被2整除
        byte[] array = new byte[hexStr.length() / 2];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) (0xff & Integer.parseInt(hexStr.substring(i * 2, i * 2 + 2), 16));
            }

            hexStr = new String(array, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return hexStr;
    }


    public static void main(String[] args) throws Exception {
        String xml = "<TowerMonitor>\n" +
                "    <Station>\n" +
                "        <StationInfo>吉-001|吉林省广播电视局技术中心台|吉林省技术中心台</StationInfo>\n" +
                "        <Tower>\n" +
                "            <TowerInfo>1|单频塔||</TowerInfo>\n" +
                "            <Second>\n" +
                "                <Param>1|OE_1_1|1#拉线塔顶东西位移|REAL|2023/5/20 14:21:07|2023/5/20 14:21:07|mm|70.3|70.3|70.3|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|OS_1_1|1#拉线塔顶南北位移|REAL|2023/5/20 14:21:07|2023/5/20 14:21:07|mm|47.4|47.4|47.4|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|REAL|2023/5/20 14:21:07|2023/5/20 14:21:07|mm|84.7|84.7|84.7|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|P_1_1|1#拉线塔顶层拉线拉力-正南|REAL|2023/5/20 14:21:07|2023/5/20\n" +
                "                    14:21:07|MPa|132.2|132.2|132.2|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|P_1_2|1#拉线塔顶层拉线拉力-西北|REAL|2023/5/20 14:21:07|2023/5/20\n" +
                "                    14:21:07|MPa|132.2|132.2|132.2|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|P_1_3|1#拉线塔顶层拉线拉力-东北|REAL|2023/5/20 14:21:07|2023/5/20\n" +
                "                    14:21:07|MPa|132.2|132.2|132.2|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|P_1_1-3_Max|1#拉线塔顶层拉线最大拉力|REAL|2023/5/20 14:21:07|2023/5/20\n" +
                "                    14:21:07|MPa|132.2|132.2|132.2|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|P_1_1-3_DV|1#拉线塔顶层拉线拉力差|REAL|2023/5/20 14:21:07|2023/5/20\n" +
                "                    14:21:07|MPa|0.1|0.1|0.1|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>1|Risk1|1#拉线塔综合风险值|REAL|2023/5/20 14:21:08|2023/5/20 14:21:08||3.9|3.9|3.9|0.0|0.0|0.0</Param>\n" +
                "            </Second>\n" +
                "            <Day>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/20 0:00:00|2023/5/20 14:21:07|mm|36.9|76.3|128.2|91.3|220.4|14.8\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/19 0:00:00|2023/5/19 23:59:59|mm|30.1|81.8|141.5|111.4|294.2|17.2\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/18 0:00:00|2023/5/18 23:59:59|mm|25.2|72.8|137.3|112.1|445.1|21.1\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/17 0:00:00|2023/5/17 23:59:58|mm|11.8|61.7|109.9|98.1|427.6|20.7\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/16 0:00:00|2023/5/16 23:59:59|mm|33.5|77.4|117.9|84.4|205.4|14.3\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/15 0:00:01|2023/5/15 23:59:59|mm|16.7|71.7|131.4|114.7|377.3|19.4\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/14 0:00:00|2023/5/14 23:59:59|mm|19.6|66.8|109.4|89.8|393.3|19.8\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/13 0:00:00|2023/5/13 23:59:59|mm|28.9|65.9|108.7|79.8|269.1|16.4\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/12 0:00:00|2023/5/12 23:59:59|mm|28.3|63.7|105.6|77.3|344.9|18.6\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/11 0:00:00|2023/5/11 23:59:59|mm|24.9|59.6|98.5|73.6|395.6|19.9\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/10 0:00:00|2023/5/10 23:59:59|mm|14.4|45.9|94.8|80.4|445.7|21.1\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/9 0:00:00|2023/5/9 23:59:59|mm|12.8|48.5|97.8|85.0|266.5|16.3\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|1#拉线塔顶整体位移|DAY|2023/5/8 0:00:00|2023/5/8 23:59:59|mm|26.2|64.5|95.0|68.8|359.0|18.9\n" +
                "                </Param>\n" +
                "            </Day>\n" +
                "            <Comparison>\n" +
                "                <Param>1|O_1_1|塔顶整体位移|N2N|2022/11/16 10:02:00|2022/11/16 10:02:00|mm|-0.4|0.0|0.0|0.0|-0.0062|-0.0031\n" +
                "                </Param>\n" +
                "                <Param>1|O_1_1|塔顶整体位移|H2H|2022/11/16 9:00:00|2022/11/16 9:00:00|mm|0.2|0.0|0.0|0.0|0.0072|0.0036</Param>\n" +
                "                <Param>1|O_1_1|塔顶整体位移|D2D|2022/11/15 7:17:47|2022/11/15 7:17:47|mm|0.1|0.0|0.0|0.0|-0.0147|-0.0074\n" +
                "                </Param>\n" +
                "                <Param>1|V_1_1|塔顶垂直度|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|mm|-0.4|0.4|1.1|1.0|1.0000|1.0000</Param>\n" +
                "                <Param>1|V_2_1|一平台垂直度|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|mm|-0.9|-0.6|0.0|1.0|1.0000|1.0000\n" +
                "                </Param>\n" +
                "                <Param>1|V_3_1|二平台垂直度|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|mm|-0.7|2.7|5.7|1.0|1.0000|1.0000\n" +
                "                </Param>\n" +
                "                <Param>1|V_4_1|三平台垂直度|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|mm|-1.0|-0.4|0.5|1.0|1.0000|1.0000\n" +
                "                </Param>\n" +
                "                <Param>1|V_5_1|四平台垂直度|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|mm|-0.5|1.1|2.4|1.0|1.0000|1.0000\n" +
                "                </Param>\n" +
                "                <Param>1|VI_1_1|塔顶振动周期|H2H|2022/11/16 9:02:25|2022/11/16 9:02:25|s|0.0|0.0|0.1|2.0|6.0000|1.6458</Param>\n" +
                "                <Param>1|VI_1_1|塔顶振动周期|D2D|2022/11/15 7:17:48|2022/11/15 7:17:48|s|-0.1|0.0|0.0|1.0|0.6404|0.2800\n" +
                "                </Param>\n" +
                "                <Param>1|F_1_1-8_Max|148最大应力|H2H|2022/11/16 9:02:25|2022/11/16\n" +
                "                    9:02:25|MPa|0.0|0.0|0.0|0.6|1.3927|0.5468\n" +
                "                </Param>\n" +
                "                <Param>1|F_1_1-8_Max|148最大应力|D2D|2022/11/15 7:17:49|2022/11/15\n" +
                "                    7:17:49|MPa|0.0|0.0|0.0|0.5|0.1985|0.0947\n" +
                "                </Param>\n" +
                "                <Param>1|F_1_1-8_DV|148杆件应力差|H2H|2022/11/16 9:02:25|2022/11/16\n" +
                "                    9:02:25|MPa|-0.4|0.0|0.1|1.6|4.0858|1.2552\n" +
                "                </Param>\n" +
                "                <Param>1|F_1_1-8_DV|148杆件应力差|D2D|2022/11/15 7:17:49|2022/11/15\n" +
                "                    7:17:49|MPa|-0.2|0.0|0.0|0.4|0.0616|0.0303\n" +
                "                </Param>\n" +
                "                <Param>1|F_2_1-8_Max|120最大应力|H2H|2022/11/16 9:02:25|2022/11/16\n" +
                "                    9:02:25|MPa|0.1|0.0|0.0|-0.6|-0.9021|-0.6871\n" +
                "                </Param>\n" +
                "                <Param>1|F_2_1-8_Max|120最大应力|D2D|2022/11/15 7:17:49|2022/11/15\n" +
                "                    7:17:49|MPa|-0.1|0.0|0.0|1.3|1.2607|0.5035\n" +
                "                </Param>\n" +
                "                <Param>1|F_2_1-8_DV|120杆件应力差|H2H|2022/11/16 9:02:25|2022/11/16\n" +
                "                    9:02:25|MPa|0.2|0.0|0.0|-0.2|-0.6078|-0.3737\n" +
                "                </Param>\n" +
                "                <Param>1|F_2_1-8_DV|120杆件应力差|D2D|2022/11/15 7:17:49|2022/11/15\n" +
                "                    7:17:49|MPa|-0.3|0.0|0.1|0.8|1.2080|0.4859\n" +
                "                </Param>\n" +
                "                <Param>1|S_1_1-6_DV|最大沉降差|N2N|2022/11/16 10:02:25|2022/11/16\n" +
                "                    10:02:25|MPa|0.2|0.2|0.2|0.0|1.0000|1.0000\n" +
                "                </Param>\n" +
                "                <Param>1|S_1_1-6_DV|最大沉降差|H2H|2022/11/16 9:00:25|2022/11/16 9:00:25|MPa|-0.4|0.0|0.0|0.3|0.2786|0.1308\n" +
                "                </Param>\n" +
                "                <Param>1|S_1_1-6_DV|最大沉降差|D2D|2022/11/15 7:17:49|2022/11/15 7:17:49|MPa|-0.4|0.0|0.0|0.3|0.4914|0.2213\n" +
                "                </Param>\n" +
                "                <Param>1|Risk1|综合风险值|N2N|2022/11/16 10:02:00|2022/11/16 10:02:00||0.0|1.2|0.0|0.0|0.9237|0.3870</Param>\n" +
                "                <Param>1|Risk1|综合风险值|H2H|2022/11/16 9:00:00|2022/11/16 9:00:00||0.0|-0.1|-0.1|-0.1|-0.1267|-0.0655\n" +
                "                </Param>\n" +
                "                <Param>1|Risk1|综合风险值|D2D|2022/11/15 7:17:49|2022/11/15 7:17:49||0.0|0.0|0.1|0.1|0.0882|0.0432</Param>\n" +
                "            </Comparison>\n" +
                "            <TextParam>\n" +
                "                <Param>1|F_1_1-8_MaxName|148最大应力位置|REAL|2022/11/16 10:02:25|2022/11/16\n" +
                "                    10:02:25||北主柱|北主柱|北主柱|北主柱|北主柱|北主柱\n" +
                "                </Param>\n" +
                "                <Param>1|F_2_1-8_MaxName|120最大应力位置|REAL|2022/11/16 10:02:25|2022/11/16\n" +
                "                    10:02:25||西北主柱|西北主柱|西北主柱|西北主柱|西北主柱|西北主柱\n" +
                "                </Param>\n" +
                "            </TextParam>\n" +
                "            <Alert>\n" +
                "                <Message>|M_01_01_20230427154725|发生塔区大风二级预警。|2023/4/27 15:47:25|尊敬的负责人：2023年4月27日\n" +
                "                    15:47:25出现塔区大风二级预警，其中塔区风速一分钟平均值大于(＞)10.0m/s（预警时结果=10.4m/s），关闭塔区入口，禁止进入施工，严防火灾发生。\n" +
                "                </Message>\n" +
                "                <Message>|M_01_01_20230311161004|发生塔区大风二级预警。|2023/3/11 16:10:04|尊敬的负责人：2023年3月11日\n" +
                "                    16:10:04出现塔区大风二级预警，其中塔区风速一分钟平均值大于(＞)10.0m/s（预警时结果=10.1m/s），关闭塔区入口，禁止进入施工，严防火灾发生。\n" +
                "                </Message>\n" +
                "            </Alert>\n" +
                "            <Config>\n" +
                "                <Title>1|某台吉塔健康监测|黑体|40|0|16777184</Title>\n" +
                "                <Theme>|0|</Theme>\n" +
                "                <Video>1|四平台上视频|192.168.3.110|554|1|admin|TowerHealth|海康威视</Video>\n" +
                "            </Config>\n" +
                "        </Tower>\n" +
                "        <Tower>\n" +
                "            <TowerInfo>|双频塔||</TowerInfo>\n" +
                "            <Second>\n" +
                "            </Second>\n" +
                "            <Comparison>\n" +
                "            </Comparison>\n" +
                "            <TextParam>\n" +
                "            </TextParam>\n" +
                "            <Alert>\n" +
                "            </Alert>\n" +
                "            <Config>\n" +
                "                <Title>|某台发射塔健康监测|黑体|40|0|16777184</Title>\n" +
                "                <Theme>|0|</Theme>\n" +
                "            </Config>\n" +
                "        </Tower>\n" +
                "        <Tower>\n" +
                "            <TowerInfo>2|2#单频塔||</TowerInfo>\n" +
                "            <Second>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|REAL|2023/5/20 14:21:52|2023/5/20 14:21:52|mm|12.9|12.9|12.9|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|REAL|2023/5/20 14:21:52|2023/5/20 14:21:52|mm|10.1|10.1|10.1|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|REAL|2023/5/20 14:21:52|2023/5/20 14:21:52|mm|16.4|16.4|16.4|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|P_2_1|2#拉线塔顶层拉线拉力-正南|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52|MPa|130.1|130.1|130.1|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|P_2_2|2#拉线塔顶层拉线拉力-西北|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52|MPa|133.9|133.9|133.9|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|P_2_3|2#拉线塔顶层拉线拉力-东北|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52|MPa|131.9|131.9|131.9|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|P_2_1-3_Max|2#拉线塔顶层拉线最大拉力|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52|MPa|133.9|133.9|133.9|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|P_2_1-3_DV|2#拉线塔顶层拉线拉力差|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52|MPa|3.8|3.8|3.8|0.0|0.0|0.0\n" +
                "                </Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|REAL|2023/5/20 14:21:52|2023/5/20 14:21:52||3.1|3.1|3.1|0.0|0.0|0.0</Param>\n" +
                "            </Second>\n" +
                "            <Comparison>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|N2N|2023/5/20 14:20:00|2023/5/20\n" +
                "                    14:20:00|mm|-0.2|-0.3|-0.1|-0.1|-0.1758|-0.0921\n" +
                "                </Param>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|H2H|2023/5/20 13:00:00|2023/5/20\n" +
                "                    13:00:00|mm|-0.4|0.1|0.0|0.0|-0.0484|-0.0245\n" +
                "                </Param>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|D2D|2023/5/19|2023/5/19|mm|0.0|0.5|-0.2|-0.1|0.8375|0.3555</Param>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|W2W|2023/5/1 23:59:58|2023/5/7 23:59:58|mm|0.0|0.4|0.5|0.3|0.2478|0.1171\n" +
                "                </Param>\n" +
                "                <Param>2|OS_2_1|2#拉线塔顶南北位移|M2M|2023/4/1|2023/4/30|mm|0.0|-0.8|0.6|0.3|0.3084|0.1439</Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|N2N|2023/5/20 14:20:00|2023/5/20 14:20:00|mm|-0.1|0.0|0.0|0.0|0.3423|0.1586\n" +
                "                </Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|H2H|2023/5/20 13:00:00|2023/5/20\n" +
                "                    13:00:00|mm|-0.1|0.0|0.0|0.0|-0.0194|-0.0098\n" +
                "                </Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|D2D|2023/5/19|2023/5/19|mm|0.0|0.3|0.0|0.0|0.3407|0.1579</Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|W2W|2023/5/1 23:59:58|2023/5/7 23:59:58|mm|0.6|0.3|0.4|0.5|0.3528|0.1631\n" +
                "                </Param>\n" +
                "                <Param>2|OE_2_1|2#拉线塔顶东西位移|M2M|2023/4/1|2023/4/30|mm|-0.2|0.5|0.5|0.1|0.0456|0.0225</Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|N2N|2023/5/20 14:20:00|2023/5/20 14:20:00|mm|-0.5|-0.1|0.0|0.3|0.9295|0.3891\n" +
                "                </Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|H2H|2023/5/20 13:00:00|2023/5/20\n" +
                "                    13:00:00|mm|-0.1|0.1|0.0|0.0|-0.1189|-0.0613\n" +
                "                </Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|D2D|2023/5/19|2023/5/19|mm|2.0|0.4|-0.1|-0.1|0.2048|0.0976</Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|W2W|2023/5/1 23:59:58|2023/5/7 23:59:58|mm|0.0|0.1|0.2|0.2|0.2268|0.1076\n" +
                "                </Param>\n" +
                "                <Param>2|O_2_1|2#拉线塔顶整体位移|M2M|2023/4/1|2023/4/30|mm|0.0|-0.1|0.3|0.3|0.2905|0.1360</Param>\n" +
                "                <Param>2|V_2_1|2#拉线塔顶垂直度|H2H|2023/5/16 13:35:40|2023/5/16\n" +
                "                    13:35:40|mm|-0.3|-0.3|-0.3|0.0|-0.7739|-0.5245\n" +
                "                </Param>\n" +
                "                <Param>2|V_2_1|2#拉线塔顶垂直度|D2D|2023/5/19 1:07:13|2023/5/19 1:07:13|mm|42.0|0.9|0.3|0.1|0.2329|0.1103\n" +
                "                </Param>\n" +
                "                <Param>2|V_2_1|2#拉线塔顶垂直度|W2W|2023/5/1 23:02:42|2023/5/7 23:02:42|mm|-0.5|0.3|0.3|0.4|0.6180|0.2720\n" +
                "                </Param>\n" +
                "                <Param>2|V_2_1|2#拉线塔顶垂直度|M2M|2023/4/1|2023/4/30|mm|-0.3|-0.3|0.1|0.1|-0.2002|-0.1057</Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|N2N|2023/5/20 14:20:00|2023/5/20 14:20:00||1.0|0.1|0.0|0.0|-0.0931|-0.0477\n" +
                "                </Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|H2H|2023/5/20 13:00:00|2023/5/20 13:00:00||0.0|0.0|0.0|0.0|-0.0214|-0.0108\n" +
                "                </Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|D2D|2023/5/19|2023/5/19||0.0|0.0|0.0|0.0|-0.0023|-0.0011</Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|W2W|2023/5/1 23:59:59|2023/5/7 23:59:59||0.0|0.4|0.0|0.0|-0.2431|-0.1300\n" +
                "                </Param>\n" +
                "                <Param>2|Risk2|2#拉线塔综合风险值|M2M|2023/4/1|2023/4/30||0.0|0.1|0.0|0.0|-0.0596|-0.0303</Param>\n" +
                "            </Comparison>\n" +
                "            <TextParam>\n" +
                "                <Param>2|P_2_1-3_MaxName|2#拉线塔顶层拉线最大拉力拉线|REAL|2023/5/20 14:21:52|2023/5/20\n" +
                "                    14:21:52||西北拉线|西北拉线|西北拉线|西北拉线|西北拉线|西北拉线\n" +
                "                </Param>\n" +
                "            </TextParam>\n" +
                "            <Alert>\n" +
                "                <Message>2|R_02_002_20230426000000|2#单频塔发生健康状态不稳定二级预警。|2023/4/26|尊敬的负责人：2#单频塔2023年4月26日\n" +
                "                    0:00:00出现健康状态不稳定二级预警，其中2#拉线塔综合风险值方差天比大于(＞)30.00%（预警时结果=100.00%），密切关注各参数预警信息，按信息提示进行处理。\n" +
                "                </Message>\n" +
                "                <Message>2|R_02_002_20230330000000|2#单频塔发生健康状态不稳定二级预警。|2023/3/30|尊敬的负责人：2#单频塔2023年3月30日\n" +
                "                    0:00:00出现健康状态不稳定二级预警，其中2#拉线塔综合风险值方差天比大于(＞)30.00%（预警时结果=100.00%），密切关注各参数预警信息，按信息提示进行处理。\n" +
                "                </Message>\n" +
                "            </Alert>\n" +
                "            <Config>\n" +
                "                <Title>2|某台2#单频塔健康监测|黑体|40|0|16777184</Title>\n" +
                "                <Theme>|0|</Theme>\n" +
                "            </Config>\n" +
                "        </Tower>\n" +
                "        <Tower>\n" +
                "            <TowerInfo>|多塔||</TowerInfo>\n" +
                "            <Second>\n" +
                "            </Second>\n" +
                "            <Comparison>\n" +
                "            </Comparison>\n" +
                "            <TextParam>\n" +
                "            </TextParam>\n" +
                "            <Alert>\n" +
                "                <Message>|M_01_01_20230427154725|发生塔区大风二级预警。|2023/4/27 15:47:25|尊敬的负责人：2023年4月27日\n" +
                "                    15:47:25出现塔区大风二级预警，其中塔区风速一分钟平均值大于(＞)10.0m/s（预警时结果=10.4m/s），关闭塔区入口，禁止进入施工，严防火灾发生。\n" +
                "                </Message>\n" +
                "                <Message>|M_01_01_20230311161004|发生塔区大风二级预警。|2023/3/11 16:10:04|尊敬的负责人：2023年3月11日\n" +
                "                    16:10:04出现塔区大风二级预警，其中塔区风速一分钟平均值大于(＞)10.0m/s（预警时结果=10.1m/s），关闭塔区入口，禁止进入施工，严防火灾发生。\n" +
                "                </Message>\n" +
                "            </Alert>\n" +
                "            <Config>\n" +
                "                <Title>|某台发射塔健康监测|黑体|40|0|16777184</Title>\n" +
                "                <Theme>|0|</Theme>\n" +
                "            </Config>\n" +
                "        </Tower>\n" +
                "    </Station>\n" +
                "</TowerMonitor>\n";
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        Map<String, Object> map = xmlToMap(doc.getDocumentElement());
        System.out.println(map);
    }
}