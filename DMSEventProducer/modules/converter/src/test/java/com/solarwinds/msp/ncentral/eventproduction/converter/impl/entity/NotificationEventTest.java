package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.NotificationOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class NotificationEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        NotificationOuterClass.Notification expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();

        //<editor-fold desc="Valid notification event">
        parsingAssertMessage = "Valid notification event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("notificationid", "474813330");
        entity1.put("method", "Email");
        entity1.put("sender", "supportteam@n-able.com");
        entity1.put("receiver", "SWMSPTechnicalSupportApplicationEngineers@solarwinds.com");
        entity1.put("body",
                " <style>         table,th,td, .tabledState               {                       font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif;                    border-collapse:collaps\n"
                        + "e;                       padding: 4px;                   margin-left: 12px;                      text-align: left ;                      font-size: 12px;                        width: 40%;             }.co\n"
                        + "louredStatenormal{  font-weight:bold;       color: green;}.colouredStatefailed{     font-weight:bold;       color: red;}.colouredStatewarning{      font-weight:bold;       color: orange;}\n"
                        + "       </style>                <p class=\"device\" style=\"font-color: white; font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: white; font-size: 22px; width: 100%; margin-left: 10px; margin\n"
                        + "-right: 4px; vertical-align: middle; padding-top: 5px; padding-right: 30px; padding-bottom: 3px; padding-left: 30px; background-color: #4aa3d6;\"><b>Customer: </b>Ruban<br /><b>Device: </b>LAPTOP-PT8ESN6I -\n"
                        + " Windows</p><b><b>           <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; m\n"
                        + "argin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Service Summary</p>               <table style=\"font-family: My\n"
                        + "riad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px;\n"
                        + "width: 40%;\" width=\"100%\">                    <tbody><tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right:\n"
                        + "4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Service</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans\n"
                        + "-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Probe Status - </td></tr>\n"
                        + "                       <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding\n"
                        + "-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Metric that triggered the notification</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,san\n"
                        + "s-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">probe check-in interval<\n"
                        + "/td></tr>                       <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px\n"
                        + "; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>State Transition</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; bord\n"
                        + "er-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><span class=\"colouredStatewarning\">Wa\n"
                        + "rning</span> to <span class=\"colouredStatefailed\">Failed</span></td></tr>                </tbody></table>                <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif\n"
                        + "; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-botto\n"
                        + "m: 3px; padding-left: 30px;\">Service Metrics</p>       <table class=\"tabledState\">\n"
                        + "                    | <tr><td class=\"tabledState\"><b>Probe check-in interval</b></td><td class=\"tabledState\">13.00 min</td></tr></table>              <p class=\"header\" style=\"font-family: Myriad,Helvetica,\n"
                        + "Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; pa\n"
                        + "dding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Notification Summary</p>          <table style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bot\n"
                        + "tom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\" width=\"100%\">            <tbody><tr><td align=\"left\" style=\"font-family\n"
                        + ": Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12\n"
                        + "px; width: 40%;\"><b>Time of State Transition</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px\n"
                        + "; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">2019-06-12 14:10:01</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tah\n"
                        + "oma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Mo\n"
                        + "nitored By</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left:\n"
                        + "4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">ashbury.n-able.com</td></tr>             <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; bor\n"
                        + "der-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Time of Notification Acknowledgem\n"
                        + "ent </b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; m\n"
                        + "argin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-col\n"
                        + "lapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Acknowledged By </b></td><td align=\"righ\n"
                        + "t\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align\n"
                        + ": left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom\n"
                        + ": 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Profile</b></td><td align=\"right\" style=\"font-family: Myri\n"
                        + "ad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; wi\n"
                        + "dth: 40%;\">Agent/Probe Failure</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4\n"
                        + "px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Trigger</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Ari\n"
                        + "al,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Agent/Probe\n"
                        + "Status</td></tr>             </tbody></table><br />                                  <p align=\"left\" class=\"rcLink\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; padding-top: 4px; pad\n"
                        + "ding-left: 4px; padding-bottom: 4px; padding-right: 4px; text-align: left; font-size: 12px;\">To remotely access this device, <a href=\"https://ashbury.n-able.com/deepLinkAction.do?method=deviceRC&amp;custom\n"
                        + "erID=567&amp;deviceID=384053504&amp;language=en_US\">click here.</a></p>                    </b></b>\n"
                        + "                    |\n" + "                    | ----------------------\n"
                        + "                    |\n"
                        + "                    | <style>         table,th,td, .tabledState               {                       font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif;                    border-collapse:collaps\n"
                        + "e;                       padding: 4px;                   margin-left: 12px;                      text-align: left ;                      font-size: 12px;                        width: 40%;             }.co\n"
                        + "louredStatenormal{  font-weight:bold;       color: green;}.colouredStatefailed{     font-weight:bold;       color: red;}.colouredStatewarning{      font-weight:bold;       color: orange;}\n"
                        + "       </style>                <p class=\"device\" style=\"font-color: white; font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: white; font-size: 22px; width: 100%; margin-left: 10px; margin\n"
                        + "-right: 4px; vertical-align: middle; padding-top: 5px; padding-right: 30px; padding-bottom: 3px; padding-left: 30px; background-color: #4aa3d6;\"><b>Customer: </b>Ruban<br /><b>Device: </b>DESKTOP-P132LCS -\n"
                        + " Windows</p><b><b>           <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; m\n"
                        + "argin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Service Summary</p>               <table style=\"font-family: My\n"
                        + "riad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px;\n"
                        + "width: 40%;\" width=\"100%\">                    <tbody><tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right:\n"
                        + "4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Service</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans\n"
                        + "-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Probe Status - </td></tr>\n"
                        + "                       <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding\n"
                        + "-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Metric that triggered the notification</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,san\n"
                        + "s-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">probe check-in interval<\n"
                        + "/td></tr>                       <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px\n"
                        + "; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>State Transition</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; bord\n"
                        + "er-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><span class=\"colouredStatewarning\">Wa\n"
                        + "rning</span> to <span class=\"colouredStatefailed\">Failed</span></td></tr>                </tbody></table>                <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif\n"
                        + "; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-botto\n"
                        + "m: 3px; padding-left: 30px;\">Service Metrics</p>       <table class=\"tabledState\">\n"
                        + "                    | <tr><td class=\"tabledState\"><b>Probe check-in interval</b></td><td class=\"tabledState\">11.00 min</td></tr></table>              <p class=\"header\" style=\"font-family: Myriad,Helvetica,\n"
                        + "Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; pa\n"
                        + "dding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Notification Summary</p>          <table style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bot\n"
                        + "tom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\" width=\"100%\">            <tbody><tr><td align=\"left\" style=\"font-family\n"
                        + ": Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12\n"
                        + "px; width: 40%;\"><b>Time of State Transition</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px\n"
                        + "; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">2019-06-12 11:18:57</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tah\n"
                        + "oma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Mo\n"
                        + "nitored By</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left:\n"
                        + "4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">ashbury.n-able.com</td></tr>             <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; bor\n"
                        + "der-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Time of Notification Acknowledgem\n"
                        + "ent </b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; m\n"
                        + "argin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-col\n"
                        + "lapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Acknowledged By </b></td><td align=\"righ\n"
                        + "t\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align\n"
                        + ": left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom\n"
                        + ": 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Profile</b></td><td align=\"right\" style=\"font-family: Myri\n"
                        + "ad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; wi\n"
                        + "dth: 40%;\">Agent/Probe Failure</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4\n"
                        + "px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Trigger</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Ari\n"
                        + "al,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Agent/Probe\n"
                        + "Status</td></tr>             </tbody></table><br />                                  <p align=\"left\" class=\"rcLink\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; padding-top: 4px; pad\n"
                        + "ding-left: 4px; padding-bottom: 4px; padding-right: 4px; text-align: left; font-size: 12px;\">To remotely access this device, <a href=\"https://ashbury.n-able.com/deepLinkAction.do?method=deviceRC&amp;custom\n"
                        + "erID=567&amp;deviceID=1697521194&amp;language=en_US\">click here.</a></p>                   </b></b>\n"
                        + "                    |\n" + "                    | ----------------------\n"
                        + "                    |\n"
                        + "                    | <style>         table,th,td, .tabledState               {                       font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif;                    border-collapse:collaps\n"
                        + "e;                       padding: 4px;                   margin-left: 12px;                      text-align: left ;                      font-size: 12px;                        width: 40%;             }.co\n"
                        + "louredStatenormal{  font-weight:bold;       color: green;}.colouredStatefailed{     font-weight:bold;       color: red;}.colouredStatewarning{      font-weight:bold;       color: orange;}\n"
                        + "       </style>                <p class=\"device\" style=\"font-color: white; font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: white; font-size: 22px; width: 100%; margin-left: 10px; margin\n"
                        + "-right: 4px; vertical-align: middle; padding-top: 5px; padding-right: 30px; padding-bottom: 3px; padding-left: 30px; background-color: #4aa3d6;\"><b>Customer: </b>Ruban<br /><b>Device: </b>SERVER - Windows<\n"
                        + "/p><b><b>            <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-ri\n"
                        + "ght: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Service Summary</p>               <table style=\"font-family: Myriad,Hel\n"
                        + "vetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 4\n"
                        + "0%;\" width=\"100%\">                    <tbody><tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; pad\n"
                        + "ding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Service</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif;\n"
                        + "border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Probe Status - </td></tr>\n"
                        + "               <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4\n"
                        + "px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Metric that triggered the notification</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif;\n"
                        + " border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">probe check-in interval</td></tr\n"
                        + ">                       <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; paddin\n"
                        + "g-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>State Transition</b></td><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-colla\n"
                        + "pse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><span class=\"colouredStatewarning\">Warning</s\n"
                        + "pan> to <span class=\"colouredStatefailed\">Failed</span></td></tr>                </tbody></table>                <p class=\"header\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; color:\n"
                        + " #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; padding-top: 5px; padding-bottom: 3px;\n"
                        + "padding-left: 30px;\">Service Metrics</p>       <table class=\"tabledState\">\n"
                        + "                    | <tr><td class=\"tabledState\"><b>Probe check-in interval</b></td><td class=\"tabledState\">11.00 min</td></tr></table>              <p class=\"header\" style=\"font-family: Myriad,Helvetica,\n"
                        + "Tahoma,Arial,clean,sans-serif; color: #fff; font-size: 20px; background-color: #243f76; width: 100%; margin-left: 10px; margin-right: 4px; vertical-align: middle; font-weight: bold; padding-right: 30px; pa\n"
                        + "dding-top: 5px; padding-bottom: 3px; padding-left: 30px;\">Notification Summary</p>          <table style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bot\n"
                        + "tom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\" width=\"100%\">            <tbody><tr><td align=\"left\" style=\"font-family\n"
                        + ": Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12\n"
                        + "px; width: 40%;\"><b>Time of State Transition</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px\n"
                        + "; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">2019-04-01 12:00:09</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tah\n"
                        + "oma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Mo\n"
                        + "nitored By</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left:\n"
                        + "4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">ashbury.n-able.com</td></tr>             <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; bor\n"
                        + "der-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Time of Notification Acknowledgem\n"
                        + "ent </b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; m\n"
                        + "argin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-col\n"
                        + "lapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Acknowledged By </b></td><td align=\"righ\n"
                        + "t\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align\n"
                        + ": left; font-size: 12px; width: 40%;\">[Not Acknowledged]</td></tr>         <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom\n"
                        + ": 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Profile</b></td><td align=\"right\" style=\"font-family: Myri\n"
                        + "ad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; wi\n"
                        + "dth: 40%;\">Agent/Probe Failure</td></tr>            <tr><td align=\"left\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4\n"
                        + "px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\"><b>Notification Trigger</b></td><td align=\"right\" style=\"font-family: Myriad,Helvetica,Tahoma,Ari\n"
                        + "al,clean,sans-serif; border-collapse: collapse; padding-bottom: 4px; padding-right: 4px; padding-top: 4px; padding-left: 4px; margin-left: 12px; text-align: left; font-size: 12px; width: 40%;\">Agent/Probe\n"
                        + "Status</td></tr>             </tbody></table><br />                                  <p align=\"left\" class=\"rcLink\" style=\"font-family: Myriad,Helvetica,Tahoma,Arial,clean,sans-serif; padding-top: 4px; pad\n"
                        + "ding-left: 4px; padding-bottom: 4px; padding-right: 4px; text-align: left; font-size: 12px;\">To remotely access this device, <a href=\"https://ashbury.n-able.com/deepLinkAction.do?method=deviceRC&amp;custom\n"
                        + "erID=567&amp;deviceID=1457692185&amp;language=en_US\">click here.</a></p>                   </b></b>\n"
                        + "                    |\n" + "                    | ----------------------\n"
                        + "                    |\n" + "                    |\n");
        entity1.put("subject", "Alert: Ruban LAPTOP-PT8ESN6I - Windows Probe Status Failed");
        entity1.put("bufferstateid", "4");
        entity1.put("contentheaders", "");
        entity1.put("attachment", "");
        entity1.put("timeinserted", "2019-02-21T13:51:52.068Z");
        entity1.put("timesent", "2019-02-21T13:51:52.068Z");
        entity1.put("profileid", "1696040503");
        entity1.put("iscorrelatedprofile", "f");
        entity1.put("triggerid", "1149461558");
        entity1.put("escalationid", "1992437999");
        entity1.put("recipientid", "50");
        entity1.put("customerid", "567");
        entity1.put("username", "Nable Support");
        entity1.put("notificationtype", "0");
        entity1.put("antid", "260870670");
        entity1.put("taskids", "381975391,277717834,358916240");
        entity1.put("format", "HTML");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("notificationbuffer")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = NotificationOuterClass.Notification.newBuilder()
                .setClientId(567)
                .setNotificationId(474813330)
                .setProfileId(1696040503)
                .setIsCorrelatedProfile(false)
                .setTriggerId(1149461558)
                .setSender("supportteam@n-able.com")
                .setReceiver("SWMSPTechnicalSupportApplicationEngineers@solarwinds.com")
                .setMethod("Email")
                .setTimeSent(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Notification notificationid">
        parsingAssertMessage = "Notification missing notificationid";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("notificationid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("notificationbuffer")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Notification missing sender">
        parsingAssertMessage = "Notification missing sender";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("sender");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("notificationbuffer")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Notification missing method">
        parsingAssertMessage = "Notification missing method";
        Map<String, String> entity4 = new HashMap<>(entity1);
        entity4.remove("method");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("notificationbuffer")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}