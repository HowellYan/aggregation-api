package com.atomscat.bootstrap.modules.weixincp.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocFetchMapper;
import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocIndexMapper;
import com.atomscat.bootstrap.modules.weixincp.entity.ApiParam;
import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;
import com.atomscat.bootstrap.modules.weixincp.entity.DocIndex;
import com.atomscat.bootstrap.modules.weixincp.service.OpenAPIService;
import com.atomscat.bootstrap.utils.UrlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author th158
 */
@Slf4j
@Service
public class OpenAPIServiceImpl implements OpenAPIService {

    private final static String WEIXIN_CP_BASE_URL = "https://qyapi.weixin.qq.com";

    @Autowired
    private DocIndexMapper docIndexMapper;

    @Autowired
    private DocFetchMapper docFetchMapper;

    @Override
    public String build(List<DocFetch> docFetchList) {
        OpenAPI openAPI = new OpenAPI();
        List<Tag> tagList = new ArrayList<>();
        Paths paths = new Paths();
        // 独立处理
        String[] strings = {"34656"};
        for (DocFetch docFetch : docFetchList) {
            update(docFetch.getId(), 0);
            if (strings.length > 0) {
                for (String id : strings) {
                    if (id.equals(docFetch.getDocId())) {
                        setOne(docFetch, tagList, paths);
                    }
                }
            } else {
                setOne(docFetch, tagList, paths);
            }
        }
        // setOne(docFetch, tagList, paths);
        openAPI.setPaths(paths);
        openAPI.setInfo(new Info().title("weixin").version("1.0.0").description(""));
        tagList = tagList.stream().distinct().collect(Collectors.toList());
        openAPI.setTags(tagList);
        log.info("OpenAPI json: ", JSONObject.toJSONString(openAPI));
        return JSONObject.toJSONString(openAPI);
    }



    private void setOne(DocFetch docFetch, List<Tag> tagList, Paths paths) {
        // 获取接口json
        JSONObject jsonObject = JSONObject.parseObject(docFetch.getDoc());
        if (jsonObject == null || jsonObject.getJSONObject("data") == null || jsonObject.getJSONObject("data").getJSONObject("document") == null) {
            return;
        }
        JSONObject document = jsonObject.getJSONObject("data").getJSONObject("document");
        if (document == null || document.getString("content_html") == null) {
            return;
        }
        // 获取 html 接口描述
        Document doc = Jsoup.parse(document.getString("content_html"));
        String[] docItems = doc.html().split("<h");
        if (docItems.length > 0) {
            for (String docItem : docItems) {
                try {
                    doc = Jsoup.parse("<h" + docItem);
                    setPaths(document, doc, docFetch, tagList, paths);
                } catch (Exception e) {
                    log.error("单个文档多个接口异常", e);
                }
            }
        } else {
            setPaths(document, doc, docFetch, tagList, paths);
        }
    }

    private void setPaths(JSONObject document, Document doc, DocFetch docFetch, List<Tag> tagList, Paths paths) {
        // 请求接口地址
        String apiUrl = getApiUrl(doc);
        if (apiUrl == null) {
            return;
        }
        // 接口详情对象
        Operation operation = new Operation();
        // 必须字段数组
        List<String> required = new ArrayList<>();

        // 配置 tags
        // operation.setTags(getTags(apiUrl, tagList));
        // 配置 tags
        operation.setTags(getTagsByJson(docFetch.getDocId(), tagList));
        //
        if (operation.getTags() != null && operation.getTags().size() > 0) {
            operation.addExtension("x-apifox-folder", operation.getTags().get(0));
        }
        // 标题
        operation.setSummary(document.getString("title"));
        // 请求响应 demo
        ApiParamDemo apiParamDemo = getApiParamDemo(doc);
        if (apiParamDemo != null && docFetch != null) {
            apiParamDemo.setApiDocId(docFetch.getDocId());
        }

        // 获取请求字段说明
        Map<String, ApiParam> mapReq = getReqParamDoc(doc, required);
        // 获取 url 路径参数
        operation.parameters(getParameters(apiUrl, mapReq));
        // 获取 body 请求参数
        operation.setRequestBody(getRequestBody(apiParamDemo, required, mapReq));
        // 获取响应字段说明
        Map<String, ApiParam> mapResp = getRespParamDoc(doc);
        // 获取 响应参数
        operation.setResponses(getResponses(apiParamDemo, mapResp));
        // 说明
        operation.setDescription(getDescription(doc, docFetch.getDocId()));
        PathItem path = new PathItem();
        PathItem.HttpMethod httpMethod = getHttpMethod(doc);
        if (httpMethod == null) {
            return;
        }
        path.operation(httpMethod, operation);

        if (paths.get(apiUrl.replace(WEIXIN_CP_BASE_URL, "")) == null) {
            paths.addPathItem(apiUrl.replace(WEIXIN_CP_BASE_URL, ""), path);
            update(docFetch.getId(), 1);
        } else {
            paths.addPathItem(apiUrl.replace(WEIXIN_CP_BASE_URL, "") + "&random=" + RandomUtil.randomNumbers(5), path);
            update(docFetch.getId(), 1);
        }
    }

    /**
     * 说明
     *
     * @param doc
     * @return
     */
    private String getDescription(Document doc, String docId) {
        String description = "";
        for (Element element : doc.getElementsByTag("strong")) {
            if (element.text().trim().contains("权限说明：")) {
                try {
                    if (doc.getElementsByTag("h2").size() > 0) {
                        description += "权限说明：" + element.nextElementSibling().text() + "\n";
                    } else {
                        description += "权限说明：" + element.parent().nextElementSibling().text() + "\n";
                    }
                } catch (Exception e) {
                    log.error("权限说明：", e);
                }
            } else if (element.text().trim().contains("更多说明：")) {
                try {
                    description += "更多说明：" + element.parent().nextElementSibling().text() + "\n";
                } catch (Exception e) {
                    log.error("更多说明：", e);
                }
            }
        }
        description += "\n" + getDocUrl(docId);
        return description;
    }

    /**
     * 接口文档地址
     *
     * @param docId
     * @return
     */
    private String getDocUrl(String docId) {
        QueryWrapper<DocIndex> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DocIndex::getDocId, docId);
        List<DocIndex> docIndexList = docIndexMapper.selectList(queryWrapper);
        String docUrl = "文档ID: " + docId;
        docUrl = docUrl + "\n 原文档地址：\n";
        for (DocIndex docIndex : docIndexList) {
            docUrl += "https://open.work.weixin.qq.com/api/doc/" + getDocUrlNode(docIndex.getParentId(), "" + docIndex.getCategoryId(), 0) + "\n";
        }
        return docUrl;
    }

    /**
     * @param parentId
     * @param node
     * @param i
     * @return
     */
    private String getDocUrlNode(Long parentId, String node, int i) {
        if (parentId == 0) {
            return node;
        } else {
            QueryWrapper<DocIndex> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocIndex::getCategoryId, parentId);
            List<DocIndex> docIndexList = docIndexMapper.selectList(queryWrapper);
            if (docIndexList != null && docIndexList.size() > 0) {
                DocIndex docIndex = docIndexList.get(0);
                if (i > 0 && docIndex.getParentId() != 0) {
                    node = docIndex.getParentId() + "/" + node;
                }
                return getDocUrlNode(docIndex.getParentId(), node, ++i);
            }
        }
        return node;
    }


    /**
     * 获取 url 路径参数
     *
     * @param apiUrl
     * @param mapReq
     * @return
     */
    private List<Parameter> getParameters(String apiUrl, Map<String, ApiParam> mapReq) {
        List<Parameter> parameterList = new ArrayList<>();
        for (Map.Entry<String, String> stringSet : UrlUtils.urlSplit(apiUrl).entrySet()) {
            PathParameter parameter = new PathParameter();
            parameter.setName(stringSet.getKey());
            parameter.setExample(stringSet.getValue());
            ApiParam ApiParam = mapReq.get(stringSet.getKey());
            if (ApiParam != null) {
                parameter.description(ApiParam.getDescription());
                parameter.required(ApiParam.getRequired());
            }
            parameterList.add(parameter);
        }
        return parameterList;
    }


    /**
     * 获取 body 请求参数
     *
     * @param apiParamDemo
     * @param required
     * @param mapReq
     * @return
     */
    private RequestBody getRequestBody(ApiParamDemo apiParamDemo, List<String> required, Map<String, ApiParam> mapReq) {
        RequestBody requestBody = new RequestBody();
        if (apiParamDemo != null && StrUtil.isNotBlank(apiParamDemo.getApiReq())) {
            // 例子 请求参数 todo 递归 json
            String apiReq = apiParamDemo.getApiReq();
            JSONObject apiReqJson = null;
            try {
                apiReq = apiReq.replaceAll("\"\\[", "\":[")
                        .replaceAll("\"op\":1\"userid\":\"lisi\"", "\"op\":1,\"userid\":\"lisi\"")
                        .replace("[PartyID1,PartyID2]","[\"PartyID1\",\"PartyID2\"]")
                        .replace("[TagID1,TagID2]","[\"TagID1\",\"TagID2\"]")
                        .replace("{\"agentids\":[1,2,3]\"parent_visible\":true}","{\"agentids\":[1,2,3],\"parent_visible\":true}")
                        .replace("\"sort_type\":SORT_TYPE,\"start\":START,\"limit\":LIMIT}", "\"sort_type\":\"SORT_TYPE\",\"start\":\"START\",\"limit\":\"LIMIT\"}")
                ;
                apiReqJson = JSONObject.parseObject(apiReq);
            } catch (Exception e) {
                log.error("apiReqJson: {}, {}", apiParamDemo.getApiDocId(), apiReq);
            }
            Content content = new Content();
            Schema schema = new ObjectSchema();
            // 循环 请求字段
            if (apiReqJson != null) {
                for (Map.Entry<String, Object> stringSet : apiReqJson.entrySet()) {
                    // 判断字段类型
                    Schema propertiesItem = getObjectType(stringSet.getValue());
                    // 获取字段说明
                    ApiParam apiParam = mapReq.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
                    // 子字段是数组
                    if (propertiesItem instanceof ArraySchema) {
                        ((ArraySchema) propertiesItem).setItems(getItems(stringSet.getValue(), mapReq));
                    } else if (propertiesItem instanceof ObjectSchema) {
                        // 子字段是对象
                        propertiesItem.properties(getProperties(stringSet.getValue(), mapReq));
                    }
                    // 添加字段到模式
                    schema.addProperties(stringSet.getKey(), propertiesItem);
                }
            } else {
                for (Map.Entry<String, ApiParam> stringSet : mapReq.entrySet()) {
                    Schema propertiesItem = new Schema();
                    ApiParam apiParam = mapReq.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
                    schema.addProperties(stringSet.getKey(), propertiesItem);
                }
            }
            schema.setRequired(required);
            if (apiReq.contains("&lt;xml&gt;")) {
                content.put("application/xml", new MediaType().example(apiReq).schema(schema));
            } else {
                content.put("application/json", new MediaType().example(apiReq).schema(schema));
            }
            requestBody.setContent(content);
        }
        return requestBody;
    }

    /**
     * 数组
     * @param v
     * @return
     */
    public Schema<?> getItems(Object v,  Map<String, ApiParam> descriptionMap) {
        Schema<?> schema = new ObjectSchema();
        try {
            JSONArray jsonArray = JSON.parseArray(v.toString());
            for (Object o : jsonArray) {
                schema = getObjectType(o);
                if (schema instanceof ObjectSchema) {
                    schema.properties(getProperties(o, descriptionMap));
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return schema;
    }

    /**
     *
     * @return
     */
    public Map<String, Schema> getProperties(Object o, Map<String, ApiParam> descriptionMap) {
        Map<String, Schema> map = new HashMap<>();
        try {
            JSONObject jsonObject = JSON.parseObject(o.toString());
            for (Map.Entry<String, Object> stringSet : jsonObject.entrySet()) {
                // 判断字段类型
                Schema propertiesItem = getObjectType(stringSet.getValue());
                // 获取字段说明
                ApiParam apiParam = descriptionMap.get(stringSet.getKey());
                if (apiParam != null) {
                    propertiesItem.description(apiParam.getDescription());
                }
                if (propertiesItem instanceof ArraySchema) {
                    ((ArraySchema) propertiesItem).setItems(getItems(stringSet.getValue(), descriptionMap));
                } else if (propertiesItem instanceof ObjectSchema) {
                    propertiesItem.properties(getProperties(stringSet.getValue(), descriptionMap));
                }
                // 添加字段到模式
                map.put(stringSet.getKey(), propertiesItem);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return map;
    }

    /**
     * 获取 响应 参数
     *
     * @param apiParamDemo
     * @param mapResp
     * @return
     */
    private ApiResponses getResponses(ApiParamDemo apiParamDemo, Map<String, ApiParam> mapResp) {
        ApiResponses responses = new ApiResponses();
        if (apiParamDemo != null && StrUtil.isNotBlank(apiParamDemo.getApiResp())) {
            Schema schema = new ObjectSchema();
            String apiResp = apiParamDemo.getApiResp();
            JSONObject apiRespJson = null;
            try {
                apiResp = apiResp
                        .replaceAll("｛", "{")
                        .replaceAll("｝", "}")
                        .replaceAll("，", ",")
                        .replaceAll("\"\"", "\",\"")
                        .replaceAll("\"\\[", "\":[")
                        .replace("\"size\":xxx,\"md5\"", "\"size\":\"xxx\",\"md5\"")
                        .replace("\"：\"", "\":\"")
                        .replace("\"tags\":[{\"group_name\":\"标签分组名称\",\"tag_name\":\"标签名称\",\"type\":1},\"remark_corp_name\":\"腾讯科技\",\"remark_mobiles\":[10000000003,10000000004]]}",
                                "\"tags\":[{\"group_name\":\"标签分组名称\",\"tag_name\":\"标签名称\",\"type\":1}],\"remark_corp_name\":\"腾讯科技\",\"remark_mobiles\":[10000000003,10000000004]}")
                        .replace("report_to\":{\"userids\":[\"userid1\",\"userid2\"]}\"report_type\":1",
                                "report_to\":{\"userids\":[\"userid1\",\"userid2\"]},\"report_type\":1")
                        .replace(",\"comment_num\"110,\"open_replay\":1,", ",\"comment_num\":110,\"open_replay\":1,")
                        .replace("\"pending\":10\"total_case\":1,", "\"pending\":10,\"total_case\":1,")
                        .replace("\"total_accepted\":1,\"total_solved\":1,}", "\"total_accepted\":1,\"total_solved\":1}")
                        .replace(",\"bind_status\"0,", ",\"bind_status\":0,")
                        .replace("\"order\":100\"fixed_group\":true,\"is_close\":false\"type\":1,", "\"order\":100,\"fixed_group\":true,\"is_close\":false,\"type\":1,")
                ;
                apiRespJson = JSONObject.parseObject(apiResp);
            } catch (Exception e) {
                try {
                    if ("syntax error, EOF".equals(e.getMessage())) {
                        apiResp = apiResp + "}";
                        apiRespJson = JSONObject.parseObject(apiResp);
                    }
                } catch (Exception ex) {
                    log.error("apiResp: ", ex);
                }
                log.error("apiResp: {}, {}", apiParamDemo.getApiDocId(), apiResp, e);
            }
            if (apiRespJson != null) {
                for (Map.Entry<String, Object> stringSet : apiRespJson.entrySet()) {
                    // 判断字段类型
                    Schema propertiesItem = getObjectType(stringSet.getValue());
                    // 获取字段说明
                    ApiParam apiParam = mapResp.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
                    // 子字段是数组
                    if (propertiesItem instanceof ArraySchema) {
                        ((ArraySchema) propertiesItem).setItems(getItems(stringSet.getValue(), mapResp));
                    } else if (propertiesItem instanceof ObjectSchema) {
                        // 子字段是对象
                        propertiesItem.properties(getProperties(stringSet.getValue(), mapResp));
                    }
                    // 添加字段到模式
                    schema.addProperties(stringSet.getKey(), propertiesItem);
                }
            } else {
                for (Map.Entry<String, ApiParam> stringSet : mapResp.entrySet()) {
                    Schema propertiesItem = new Schema();
                    ApiParam apiParam = mapResp.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
                    schema.addProperties(stringSet.getKey(), propertiesItem);
                }
            }
            Content content = new Content();
            content.put("application/json", new MediaType().example(apiResp).schema(schema));
            responses.addApiResponse("200", new ApiResponse().content(content));
        }
        return responses;
    }

    /**
     * 获取类型
     *
     * @param obj
     * @return
     */
    private Schema getObjectType(Object obj) {
        Schema propertiesItem = new ObjectSchema();
        if (obj instanceof String) {
            propertiesItem = new StringSchema();
        } else if (obj instanceof Integer) {
            propertiesItem = new IntegerSchema();
        } else if (obj instanceof Number) {
            propertiesItem = new NumberSchema();
        } else if (obj instanceof Iterable) {
            propertiesItem = new ArraySchema();
        } else if (obj instanceof Boolean) {
            propertiesItem = new BooleanSchema();
        } else if (obj instanceof Date) {
            propertiesItem = new DateSchema();
        } else if (obj instanceof OffsetDateTime) {
            propertiesItem = new DateTimeSchema();
        }
        return propertiesItem;
    }

    /**
     * 获取请求字段说明
     *
     * @return
     */
    private Map<String, ApiParam> getReqParamDoc(Document doc, List<String> required) {
        Map<String, ApiParam> mapReq = new HashMap<>();
        // 获取参数说明：
        Element tableElement = null;
        for (Element element : doc.getElementsByTag("thead")) {
            if (element.getElementsByTag("th").size() == 3) {
                tableElement = element.parent();
            } else if (doc.html().contains("&lt;xml&gt;")) {
                tableElement = element.parent();
            }
        }
        if (tableElement != null) {
            Element tbodyElement = tableElement.getElementsByTag("tbody").get(0);
            for (Element element : tbodyElement.getElementsByTag("tr")) {
                Elements tds = element.getElementsByTag("td");
                if (tds.size() >= 3) {
                    ApiParam ApiParam = new ApiParam();
                    ApiParam.setName(tds.get(0).text().trim());
                    if (tds.get(1).text().trim().equals("是")) {
                        ApiParam.setRequired(true);
                        required.add(ApiParam.getName());
                    }
                    ApiParam.setDescription(tds.get(2).text().trim());
                    mapReq.put(ApiParam.getName(), ApiParam);
                } else if (doc.html().contains("&lt;xml&gt;")) {
                    ApiParam ApiParam = new ApiParam();
                    ApiParam.setName(tds.get(0).text().trim());
                    ApiParam.setDescription(tds.get(1).text().trim());
                    mapReq.put(ApiParam.getName(), ApiParam);
                }
            }
        }
        return mapReq;
    }

    /**
     * 获取响应字段说明
     *
     * @param doc
     * @return
     */
    private Map<String, ApiParam> getRespParamDoc(Document doc) {
        Map<String, ApiParam> mapResp = new HashMap<>();
        //响应参数说明：
        Element tableElementResp = null;
        for (Element element : doc.getElementsByTag("thead")) {
            if (element.getElementsByTag("th").size() == 2) {
                tableElementResp = element.parent();
            }
        }

        if (tableElementResp != null) {
            Element tbodyElement = tableElementResp.getElementsByTag("tbody").get(0);
            for (Element element : tbodyElement.getElementsByTag("tr")) {
                Elements tds = element.getElementsByTag("td");
                if (tds.size() >= 2) {
                    ApiParam ApiParam = new ApiParam();
                    ApiParam.setName(tds.get(0).text().trim());
                    ApiParam.setDescription(tds.get(1).text().trim());
                    mapResp.put(ApiParam.getName(), ApiParam);
                }
            }
        }
        return mapResp;
    }

    /**
     * 获取tags
     *
     * @param apiUrl
     * @param tagList
     * @return
     */
    private List<String> getTags(String apiUrl, List<Tag> tagList) {
        String tagStr = apiUrl.replace(WEIXIN_CP_BASE_URL, "");
        tagStr = tagStr.substring(0, tagStr.lastIndexOf("/"));
        // 全局
        Tag tag = new Tag();
        tag.setDescription("");
        tag.setName(tagStr);
        tagList.add(tag);
        return Arrays.asList(tagStr);
    }

    /**
     * 接口请求类型
     */
    private PathItem.HttpMethod getHttpMethod(Document doc) {
        PathItem.HttpMethod method = null;
        for (Element element : doc.getElementsByTag("p")) {
            if (element.text().contains("PUT")) {
                method = PathItem.HttpMethod.PUT;
            } else if (element.text().contains("GET")) {
                method = PathItem.HttpMethod.GET;
            } else if (element.text().contains("HEAD")) {
                method = PathItem.HttpMethod.HEAD;
            } else if (element.text().contains("POST")) {
                method = PathItem.HttpMethod.POST;
            } else if (element.text().contains("DELETE")) {
                method = PathItem.HttpMethod.DELETE;
            } else if (element.text().contains("PATCH")) {
                method = PathItem.HttpMethod.PATCH;
            } else if (element.text().contains("OPTIONS")) {
                method = PathItem.HttpMethod.OPTIONS;
            } else if (element.text().contains("PUT")) {
                method = PathItem.HttpMethod.PUT;
            }
        }
        return method;
    }

    /**
     * 接口请求地址
     */
    private String getApiUrl(Document doc) {
        String apiUrl = null;
        for (Element element : doc.getElementsByTag("p")) {
            if (element.text().contains("请求地址")) {
                apiUrl = element.text().substring(element.text().indexOf("请求地址") + 5);
            }
        }
        if (apiUrl != null) {
            String[] strings = apiUrl.split(" ");
            for (String s : strings) {
                if (s.contains("http:") || s.contains("https:")) {
                    apiUrl = s;
                }
            }
            // ACCESS_TOKEN to {{accesstoken}}
            apiUrl = apiUrl.replace("access_token=ACCESS_TOKEN", "access_token={{accesstoken}}");

        }
        return apiUrl;
    }


    /**
     * 获取请求包体、返回结果，demo
     *
     * @param doc
     * @return
     */
    private ApiParamDemo getApiParamDemo(Document doc) {
        ApiParamDemo apiParamDemo = new ApiParamDemo();
        for (Element element : doc.getElementsByTag("code")) {
            if (element.parent() == null || element.parent().previousElementSibling() == null) {
                return null;
            }
            if (element.parent().previousElementSibling().text().contains("请求包体") || element.parent().previousElementSibling().text().contains("请求示例")) {
                //请求包体
                apiParamDemo.setApiReq(element.html().replaceAll("[\\s\\t\\n\\r]", "").replace("\\n", "").replaceAll("\",}", "\"}").trim());
            } else if (element.parent().previousElementSibling().text().contains("返回结果")) {
                //返回结果
                apiParamDemo.setApiResp(element.html().replaceAll("[\\s\\t\\n\\r]", "").replace("\\n", "").replaceAll("\",}", "\"}").replaceAll("　", "").trim());
            }
        }
        return apiParamDemo;
    }

    public List<String> getTagsByJson(String docId, List<Tag> tagList) {
        try {
            List<String> tags = new ArrayList<>();
            QueryWrapper<DocIndex> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocIndex::getDocId, docId);
            List<DocIndex> docIndexList = docIndexMapper.selectList(queryWrapper);
            if (docIndexList != null && docIndexList.size() > 0) {
                for (DocIndex docIndex : docIndexList) {
                    List<String> tagsPath = new ArrayList<>();
                    getNextNode(docIndex.getParentId(), tagsPath, 0);
                    // 去重
                    tagsPath = tagsPath.stream().distinct().collect(Collectors.toList());
                    for (String tagName : tagsPath) {
                        Tag tag = new Tag();
                        tag.setDescription("");
                        tag.setName(tagName);
                        tagList.add(tag);
                    }
                    if (tagsPath.size() > 1) {
                        String tagPath = "";
                        for (String tag : tagsPath) {
                            if (StrUtil.isBlank(tagPath)) {
                                tagPath = tag;
                            } else {
                                tagPath = tag + "/" + tagPath;
                            }
                        }
                        // tags = new ArrayList<>();
                        tagsPath.add(tagPath);
                        Tag tag = new Tag();
                        tag.setDescription("");
                        tag.setName(tagPath);
                        tagList.add(tag);
                    }
                    tags.addAll(tagsPath);
                }
            }
            // 去重
            return tags.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            log.error("tag: {}, {}", docId, e);
        }
        return null;
    }

    /**
     * 获取 tag
     * @param parentId
     * @param tags
     * @param i
     * @return
     */
    public Boolean getNextNode(Long parentId, List<String> tags, int i) {
        // 限制目录层级： 2 || i >= 2
        if (parentId == 0 ) {
            return true;
        } else {
            QueryWrapper<DocIndex> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocIndex::getCategoryId, parentId);
            List<DocIndex> docIndexList = docIndexMapper.selectList(queryWrapper);
            if (docIndexList != null && docIndexList.size() > 0) {
                for (DocIndex docIndex : docIndexList) {
                    tags.add(docIndex.getTitle());
                    //tags.add("parent_id:" + docIndex.getParentId());
                    getNextNode(docIndex.getParentId(), tags, ++i);
                }
            }
        }
        return false;
    }

    @Data
    class ApiParamDemo {
        private String apiDocId = null;
        private String apiReq = null;
        private String apiResp = null;
    }

    public void update(Long id, Integer result) {
        DocFetch docFetch = new DocFetch();
        docFetch.setId(id);
        docFetch.setResult(result);
        docFetchMapper.updateById(docFetch);
    }
}
