package com.micall.testCases;

import com.micall.entity.API;
import com.micall.entity.Cases;
import com.micall.utils.AuthorizationUtils;
import com.micall.utils.ExcelUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginCase extends BaseCase {
    @Test(dataProvider = "datas" ,description = "登录接口测试")
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
        String reqBody = call(api,cases,language,false);
        AuthorizationUtils.storeTokenAddMemberId(reqBody);
        boolean assertResponseFlag = assertResponse(cases,language,reqBody);
        String assertContent = (assertResponseFlag) ?"Pass":"Fail";
    }
    @DataProvider(name = "datas")
    public Object[][] datas(){
        Object[][] datas = ExcelUtils.getAPIandCaseByApiId("1");
        return datas;
    }
}
