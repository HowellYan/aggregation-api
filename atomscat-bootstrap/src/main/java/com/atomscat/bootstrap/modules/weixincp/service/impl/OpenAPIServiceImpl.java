package com.atomscat.bootstrap.modules.weixincp.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.weixincp.entity.ApiParam;
import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;
import com.atomscat.bootstrap.modules.weixincp.service.OpenAPIService;
import com.atomscat.bootstrap.utils.UrlUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.*;

/**
 * @author th158
 */
@Slf4j
@Service
public class OpenAPIServiceImpl implements OpenAPIService {

    private final static String WEIXIN_CP_BASE_URL = "https://qyapi.weixin.qq.com";

    @Override
    public String build(List<DocFetch> docFetchList) {
        OpenAPI openAPI = new OpenAPI();
        List<Tag> tagList = new ArrayList<>();
        Paths paths = new Paths();
        for (DocFetch docFetch : docFetchList) {
            setOne(docFetch, tagList, paths);
        }
        openAPI.setPaths(paths);
        openAPI.setInfo(new Info().title("weixin").version("1.0.0").description(""));
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
        // 标题
        operation.setSummary(document.getString("title"));
        // 请求响应 demo
        ApiParamDemo apiParamDemo = getApiParamDemo(doc);
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
        PathItem path = new PathItem();
        PathItem.HttpMethod httpMethod = getHttpMethod(doc);
        if (httpMethod == null) {
            return;
        }
        path.operation(httpMethod, operation);
        paths.addPathItem(apiUrl.replace(WEIXIN_CP_BASE_URL, ""), path);
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
                apiReqJson = JSONObject.parseObject(apiReq);
            } catch (Exception e) {
                log.error("apiReqJson: {}", apiReqJson);
            }
            Content content = new Content();
            Schema schema = new ObjectSchema();
            // 循环 请求字段
            if (apiReqJson != null) {
                for (Map.Entry<String, Object> stringSet : apiReqJson.entrySet()) {
                    Schema propertiesItem = getObjectType(stringSet.getValue());
                    ApiParam apiParam = mapReq.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
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
            content.put("application/json", new MediaType().example(apiReq).schema(schema));
            requestBody.setContent(content);
        }
        return requestBody;
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
                apiRespJson = JSONObject.parseObject(apiResp.replace("\"\"", "\",\"")
                        .replaceAll("，", ",")
                        .replaceAll(":xxx,", ":\"xxx\",")
                        .replaceAll("}", ":\"xxx\",")
                        .replaceAll("　　", "")
                );
            } catch (Exception e) {
                log.error("apiResp: {}", apiResp);
            }
            if (apiRespJson != null) {
                for (Map.Entry<String, Object> stringSet : apiRespJson.entrySet()) {
                    Schema propertiesItem = getObjectType(stringSet.getValue());
                    ApiParam apiParam = mapResp.get(stringSet.getKey());
                    if (apiParam != null) {
                        propertiesItem.description(apiParam.getDescription());
                    }
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
            if (element.text().contains("请求方式：PUT")) {
                method = PathItem.HttpMethod.PUT;
            } else if (element.text().contains("请求方式：GET")) {
                method = PathItem.HttpMethod.GET;
            } else if (element.text().contains("请求方式：HEAD")) {
                method = PathItem.HttpMethod.HEAD;
            } else if (element.text().contains("请求方式：POST")) {
                method = PathItem.HttpMethod.POST;
            } else if (element.text().contains("请求方式：DELETE")) {
                method = PathItem.HttpMethod.DELETE;
            } else if (element.text().contains("请求方式：PATCH")) {
                method = PathItem.HttpMethod.PATCH;
            } else if (element.text().contains("请求方式：OPTIONS")) {
                method = PathItem.HttpMethod.OPTIONS;
            } else if (element.text().contains("请求方式：PUT")) {
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
            if (element.text().contains("请求地址：")) {
                apiUrl = element.text().substring(element.text().indexOf("请求地址：") + 5);
            }
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
            if (element.parent().previousElementSibling().text().contains("请求包体：")) {
                //请求包体
                apiParamDemo.setApiReq(element.html().replaceAll("[\\s\\t\\n\\r]", "").replace("\\n", "").replaceAll("\",}", "\"}"));
            } else if (element.parent().previousElementSibling().text().contains("返回结果：")) {
                //返回结果
                apiParamDemo.setApiResp(element.html().replaceAll("[\\s\\t\\n\\r]", "").replace("\\n", "").replaceAll("\",}", "\"}"));
            }
        }
        return apiParamDemo;
    }

    public List<String> getTagsByJson(String docId, List<Tag> tagList) {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:catagories.json");
            String json = FileUtil.readUtf8String(jsonFile);
            JSONObject jsonObject = JSON.parseObject(json);
            List<String> tags = new ArrayList<>();
            getNextNode(jsonObject, docId, tags);
            log.info("tag: {}, {}", docId, JSON.toJSON(tags));
            for (String tagName : tags) {
                Tag tag = new Tag();
                tag.setDescription("");
                tag.setName(tagName);
                tagList.add(tag);
            }
            return tags;
        } catch (Exception e) {
            log.error("tag: {}, {}", docId, e);
        }
        return null;
    }

    public Boolean getNextNode(JSONObject jsonObject, String docId, List<String> tags) {
        Boolean res = false;
        if (jsonObject == null || jsonObject.size() <= 0) {
            return res;
        }
        if (jsonObject != null && jsonObject.getString("title") != null) {
            tags.add(jsonObject.getString("title"));
        }
        int i = 0;
        for (Map.Entry<String, Object> stringSet : jsonObject.entrySet()) {
            i++;
            if (stringSet == null || StrUtil.isBlank(stringSet.getKey()) || stringSet.getValue() == null || StrUtil.isBlank(stringSet.getValue().toString())) {
                continue;
            }
            // 找到正确
            if (stringSet.getKey().equals("doc_id") && docId.equals(stringSet.getValue().toString())) {
                res = true;
                break;
            } else {
                if (stringSet.getValue() instanceof Iterable) {
                    JSONArray jsonArray = JSON.parseArray(stringSet.getValue().toString());
                    if ("children".equals(stringSet.getKey())) {
                        for (Object obj : jsonArray) {
                            if (getNextNode(JSON.parseObject(obj.toString()), docId, tags)) {
                                res = true;
                                break;
                            }
                        }
                    }
                } else if (!(stringSet.getValue() instanceof String) &&
                        !(stringSet.getValue() instanceof Number) &&
                        !(stringSet.getValue() instanceof Boolean)
                ) {
                    JSONObject jsonNextObject = JSON.parseObject(stringSet.getValue().toString());
                    if (getNextNode(jsonNextObject, docId, tags)) {
                        res = true;
                        break;
                    }
                }
            }
            // 最后一个
            if (jsonObject.size() == i && !res) {
                if (tags.size() > 0) {
                    tags.remove(tags.size() - 1);
                }
            }
        }

        return res;
    }

    @Data
    class ApiParamDemo {
        private String apiReq = null;
        private String apiResp = null;
    }

}
