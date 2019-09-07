package com.luopo.goupiao.controller;

import com.alibaba.druid.util.StringUtils;
import com.luopo.goupiao.access.AccessLimit;
import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.mapper.StationMapper;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.redis.TrainKey;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.StationService;
import com.luopo.goupiao.service.TrainService;
import com.luopo.goupiao.util.CityUtil;
import com.luopo.goupiao.validator.IsCity;
import com.luopo.goupiao.validator.IsDate;
import com.luopo.goupiao.vo.QueryTrainDetailParamVo;
import com.luopo.goupiao.vo.QueryTrainParamVo;
import com.luopo.goupiao.vo.TrainVo;
import com.luopo.goupiao.vo.ZhongzhuanVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.groovy.GroovyMarkupConfig;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("")
public class TrainController implements InitializingBean {

    @Autowired
    TrainService trainService;

    @Autowired
    RedisService redisService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    StationService stationService;

    //系统初始化
    public void afterPropertiesSet() throws Exception {
        CityUtil.cityList = stationService.getAllCity();
        System.out.println(CityUtil.cityList);

    }

    @GetMapping(value="/trainListZhongzhuan")
    @ResponseBody
    public Result<String> listTrainZhongzhuan(HttpServletRequest request, HttpServletResponse response,
                                    Model model, User user,
                                    //参数校验注解，判断是否为非法校验侵入
                                    @Valid QueryTrainParamVo queryTrainParamVo ) {
        String fromCity = queryTrainParamVo.getFromCity();
        String toCity = queryTrainParamVo.getToCity();
        String date = queryTrainParamVo.getDate();

        if (fromCity.equals(toCity)) {
            throw new GlobalException(CodeMsg.STATION_CAN_NOT_EQUAL);
        }

        model.addAttribute("user", user);
        model.addAttribute("fromCity", fromCity);
        model.addAttribute("toCity", toCity);
        model.addAttribute("date", date);

        //取页面缓存
        String html = redisService.get(TrainKey.getTrainListPageZhongzhuan,
                ""+fromCity+"_"+toCity+"_"+date, String.class);
        if(!StringUtils.isEmpty(html)) {
//            System.out.println("    >> 直接获取TrainList页面缓存");
            Result.success(html);
        }

        //取页面缓存失败，获取数据，渲染模板，设置缓存，并返回
        //直接缓存了页面，则不需要缓存车次数据了（效果相同嘛）
        List<ZhongzhuanVo> zhongzhuanVoList = trainService.listTrainVoZhongzhuan(fromCity, toCity, date);

        if (zhongzhuanVoList != null) {
            List<TrainVo> trainList = new LinkedList<>();

            //String转sqlDate
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dateJava = null;
            java.sql.Date dateSql = null;   //注意此处是只有年与日的java.sql.Date
            try {
                dateJava = dateFormat.parse(date);
                dateSql = new java.sql.Date(dateJava.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            for (ZhongzhuanVo zhongzhuanVo : zhongzhuanVoList) {
                TrainVo trainVoFrom = trainService.getTrainZhongzhuan(zhongzhuanVo.getFirstTrainId(),
                        zhongzhuanVo.getFirstFromStationId(), zhongzhuanVo.getFirstToStationId());

                trainVoFrom = trainService.buchongTrainVo(trainVoFrom, dateSql);
                trainVoFrom.setDate(dateSql);

                TrainVo trainVoTo = trainService.getTrainZhongzhuan(zhongzhuanVo.getSecondTrainId(),
                        zhongzhuanVo.getSecondFromStationId(), zhongzhuanVo.getSecondToStationId());

                int dayNum = Integer.parseInt(trainVoTo.getFromTime().substring(0, 2));
                if (dayNum != 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dateJava);
                    c.add(Calendar.DATE, dayNum); //将当前日期加dayNum天
                    dateSql = new java.sql.Date(c.getTime().getTime());   //calendar.getTime是date
                    //date.getTime是long类型毫秒

                    String dayNumStr = String.format("%02d", dayNum);
                    trainVoTo.setFromTime(dayNumStr + trainVoTo.getFromTime().substring(2, 8));
                }

                //to的date可能已经改变了
                trainVoTo = trainService.buchongTrainVo(trainVoTo, dateSql);
                trainVoTo.setDate(dateSql);

                trainList.add(trainVoFrom);
                trainList.add(trainVoTo);
            }

            model.addAttribute("trainList", trainList);
        }
        else {
            model.addAttribute("trainList", null);
        }


        SpringWebContext springWebContext = new SpringWebContext(request, response,
                request.getServletContext(),
                request.getLocale(),
                model.asMap(),
                applicationContext);

        html = thymeleafViewResolver
                .getTemplateEngine()
                .process("train_list_zhongzhuan", springWebContext);

        //页面缓存有效时期 20 秒
        if(!StringUtils.isEmpty(html)) {
            redisService.set(TrainKey.getTrainListPageZhongzhuan,
                    ""+fromCity+"_"+toCity+"_"+date, html);
        }

        return Result.success(html);
    }

    @GetMapping(value="/trainList")
    @ResponseBody
    public Result<String> listTrain(HttpServletRequest request, HttpServletResponse response,
                            Model model, User user,
                            //参数校验注解，判断是否为非法校验侵入
                            @Valid QueryTrainParamVo queryTrainParamVo ) {
        String fromCity = queryTrainParamVo.getFromCity();
        String toCity = queryTrainParamVo.getToCity();
        String date = queryTrainParamVo.getDate();

        if (fromCity.equals(toCity)) {
            throw new GlobalException(CodeMsg.STATION_CAN_NOT_EQUAL);
        }

        model.addAttribute("user", user);
        model.addAttribute("fromCity", fromCity);
        model.addAttribute("toCity", toCity);
        model.addAttribute("date", date);

        //取页面缓存
    	String html = redisService.get(TrainKey.getTrainListPage,
                ""+fromCity+"_"+toCity+"_"+date, String.class);
    	if(!StringUtils.isEmpty(html)) {
//            System.out.println("    >> 直接获取TrainList页面缓存");
            Result.success(html);
    	}

    	//取页面缓存失败，获取数据，渲染模板，设置缓存，并返回
        //直接缓存了页面，则不需要缓存车次数据了（效果相同嘛）
        List<TrainVo> trainList = trainService.listTrainVo(fromCity, toCity, date);
        model.addAttribute("trainList", trainList);

        SpringWebContext springWebContext = new SpringWebContext(request, response,
                request.getServletContext(),
                request.getLocale(),
                model.asMap(),
                applicationContext);

        html = thymeleafViewResolver
                .getTemplateEngine()
                .process("train_list", springWebContext);

        //页面缓存有效时期 20 秒
        if(!StringUtils.isEmpty(html)) {
//            System.out.println("    >> 设置TrainList Page页面缓存");
            redisService.set(TrainKey.getTrainListPage,
                    ""+fromCity+"_"+toCity+"_"+date, html);
        }

        return Result.success(html);
    }


    //相对于trainList，这是一个用于静态页面ajax调用的方法，只返回mdoel数据
    @AccessLimit(seconds = 30, maxCount = 5)
    @GetMapping(value="/trainDetail")
    @ResponseBody
    public Result<TrainVo> detailTrain(HttpServletRequest request, HttpServletResponse response,
                                    Model model, User user,
                                    //参数校验注解，判断是否为非法校验侵入
                                    @Valid QueryTrainDetailParamVo queryTrainDetailParamVo ) {
        String fromCity = queryTrainDetailParamVo.getFromCity();
        String toCity = queryTrainDetailParamVo.getToCity();
        String date = queryTrainDetailParamVo.getDate();
        int trainId = queryTrainDetailParamVo.getTrainId();

//        System.out.println("    >> 用户 : " + user.getUserName() + " 访问了该网站");

        List<TrainVo> trainVoList = trainService.listTrainVo(fromCity, toCity, date);

        if (trainVoList == null) {
            return Result.error(CodeMsg.TRAIN_TINGYUN);
        }

        for (TrainVo it : trainVoList) {
            if (it.getTrainId() == trainId) {
                return Result.success(it);
            }
        }

        return Result.error(CodeMsg.TRAIN_NO_EXIST_ON_CONDITIONS);
    }


//    @RequestMapping(value="/trainList/{fromCity}/{toCity}/{date}")
//    @ResponseBody
//    public Result<List<TrainVo>> detail(HttpServletRequest request,
//                                  HttpServletResponse response,
//                                  Model model,
//                                  User user,
//                                  @PathVariable("fromCIty")String fromCity,
//                                  @PathVariable("toCity")String toCity,
//                                  @PathVariable("date")Date date
//                                  ) {
//
//        //先从缓存获取
//        List<TrainVo> trainList = redisService.get(TrainKey.getTrainList,
//                ""+fromCity+"_"+toCity+"_"+date, List.class);
//    	if(null != trainList) {
//    	    //成功则返回
//    		return Result.success(trainList);
//    	}
//
//    	//否则从数据库获取
//        trainList = trainService.listTrainVo(fromCity, toCity, date);
//        //并添加该数据为缓存
//        redisService.set(TrainKey.getTrainList,
//                ""+fromCity+"_"+toCity+"_"+date, List.class);
//
//        return Result.success(trainList);
//    }


}
