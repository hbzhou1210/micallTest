package com.micall.testCases;

import com.micall.constant.Constants;
import com.micall.entity.API;
import com.micall.entity.Cases;
import com.micall.utils.AuthorizationUtils;
import com.micall.utils.ExcelUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetMyInfo extends BaseCase {
    @Test(dataProvider = "datas" ,description = "获取本人信息")
    public void execute(API api , Cases cases){
        super.execute(api,cases);
    }
    @Override
    public void executeAll(API api, Cases cases,String language){
        try{
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String reqBody = call(api,cases,language,true);
        boolean assertResponseFlag = assertResponse(cases,language,reqBody);
        String assertContent = (assertResponseFlag) ?"Pass":"Fail";
        if("zh-Hans".equals(language)){
            addWBD(Integer.parseInt(cases.getCaseNumber()), Constants.ACTUAL_WAITER_BACK_CELL_NUM, reqBody);
            addWBD(Integer.parseInt(cases.getCaseNumber()),Constants.ACTUAL_result_CALL_CELL_NUM,assertContent);
            System.out.println();
        }
    }
    @DataProvider(name = "datas")
    public Object[][] datas(){
        Object[][] datas = ExcelUtils.getAPIandCaseByApiId("3");
        return datas;
    }
}
