package com.amazonaws.lambda.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

//import javayo.my_ord;

import org.json.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

public class LambdaFunctionHandler_message implements RequestHandler<JSONObject, JSONObject> {
	
	final String IDLE_BUTTON = "다시 시작하기";
	final String IDLE = "참여 마감 하였습니다! 감사합니다!";
	final String FAIL_MSG = "알럽고가 생각할 시간이 필요한가봐요! 잠시 후 다시 입력 해주세요!";
	final String ASSURE_MSG = "네! 하겠습니다!";
	final String TABLE = "MALE_PUS";
	final String FINAL_MSG = "답변 해 주셔서 감사합니다! 구체적 알럽고 이용 방법은 \"3.알럽고 사용 방법\"메뉴를, 공지 사항은 \"4.공지사항\"메뉴를 눌러 확인 해 주세요!";
	
	@Override
	public JSONObject handleRequest(JSONObject input, Context context) {
		Map<String, AttributeValue> dbItem = new HashMap<String, AttributeValue>();

		JSONObject js = new JSONObject();
		JSONArray jsArr = new JSONArray();
		JSONObject jspic = new JSONObject();
		JSONObject jspic2 = new JSONObject();

		JSONObject jsb = new JSONObject();
		//매치 관한 변수
		final int MATCH_LIMIT = 10;
		
		//개인정보 입력 화면
		final int STATE_INIT_HEIGHT = 2;
		final int STATE_INIT_AGE = 3;
		final int STATE_INIT_WISH_HEIGHT = 4;
		final int STATE_INIT_WISH_AGE = 5;
		final int STATE_INIT_SEX = 6;
		final int STATE_NORMAL = 7;

		//질문 
		final int STATE_INIT = 9;
		final int STATE_Q_1 = 10;
		final int STATE_Q_2 = 11;
		final int STATE_Q_3 = 12;
		final int STATE_Q_4 = 13;
		final int STATE_Q_5 = 14;
		final int STATE_Q_6 = 15;
		final int STATE_Q_7 = 16;
		final int STATE_Q_8 = 17;
		final int STATE_Q_9 = 18;
		final int STATE_Q_10 = 19;
		final int STATE_Q_11 = 20;
		final int STATE_Q_12 = 21;
		final int STATE_Q_13 = 22;
		final int STATE_Q_14 = 23;
		final int STATE_Q_15 = 24;
		final int STATE_Q_16 = 25;
		final int STATE_Q_17 = 26;
		final int STATE_Q_18 = 27;
		final int STATE_Q_19 = 28;
		final int STATE_Q_20 = 29;
		final int STATE_Q_21 = 30;
		final int STATE_Q_22 = 31;
		final int STATE_Q_23 = 32;
		final int STATE_Q_24 = 33;
		final int STATE_Q_LAST = 34;
		
		final int STATE_INIT_WISH_ORD_1 = 41;
		final int STATE_INIT_WISH_ORD_2 = 42;
		final int STATE_INIT_WISH_ORD_3 = 43;
		final int STATE_INIT_WISH_ORD_4 = 44;

		final int STATE_INIT_WISH_ORD = 49;
		final int STATE_CHG_COND_RDY = 50;
		final int STATE_CHG_COND = 51;
		final int ORD_ERR = 1;
		final int NO_ERR = 0;

		String Q = null, A = null, B = null, C = null, D = null, url = null, querry = null;

		// 유저가 선택하거나 입력한 메세지 = content
		// 유저 고유 키 = user_key
		String answer = input.get("content").toString();
		String user_key = input.get("user_key").toString();

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).build();
		DynamoDB dynamoDB = new DynamoDB(client);

		// 테이블 접속
		// Table table = dynamoDB.getTable("kakaoDB");
		Table table = dynamoDB.getTable("MALE_PUS");

		// 내보내야 하는 JSON이 2단계여서 객체도 2개가 필요
		JSONObject jsMes = new JSONObject();
		JSONObject jsAns = new JSONObject();

		// Message_button 구현시 객체 하나 더 필요
		JSONObject jsMes2 = new JSONObject();

		/////////////////// INIT/////////////////////////////
		/////////////////////////////////////////////////////
		Item id_item = table.getItem("id", user_key);
		if (id_item == null) {
			id_item = new Item().withPrimaryKey("id", user_key).withNumber("state", STATE_INIT)
					.withNumber("is_match", 1).withStringSet("history", " ").withNumber("available", 10)
					.withStringSet("matched", " ").withNumber("time_stamp", 0).withNumber("A", 0)
					.withNumber("B", 0).withNumber("C", 0).withNumber("D", 0);
			table.putItem(id_item);
			jsAns.put("text", "사용하실 닉네임을 입력 해 주세요!");
			jsMes.put("message", jsAns);
			return jsMes;
		}
		
		
		Integer state = id_item.getInt("state");
		switch (state) {
		
		case STATE_NORMAL: {
			// 주 메뉴 화면
			// 1. 매칭 결과 확인하기
			// 2. 이성과 매칭하기
			// 3. 알파고 사용 방법
			// 4. 공지사항
			int available = id_item.getInt("available");

			if (answer.contains("1")) {
				// 1번 눌렸을때 매칭 결과 확인하기
				Set<String> matched = id_item.getStringSet("matched");

				if (matched.contains(" ") || matched.size() == 1) {
					return main_message("현재 매칭된 사람이 없습니다!");
				}

				List nickname = new ArrayList();

				for (String opp : matched) {
					Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
					expressionAttributeValues.put(":id_match", new AttributeValue().withS(opp));

					ScanRequest scanRequest = new ScanRequest().withTableName("MALE_PUS")
							.withFilterExpression("id = :id_match").withProjectionExpression("id, nick")
							.withExpressionAttributeValues(expressionAttributeValues);
					ScanResult result = client.scan(scanRequest);

					//받는 사람의 닉네임을 출력 하는걸로 일단
					nickname.add(result.getItems().get(0).get("nick").getS());
					// jsArr.add(nickname);
				}

				return main_message(nickname.get(0) + "님과 " + nickname.get(1) + " 님이 매칭 되었습니다.");

			} else if (answer.contains(ASSURE_MSG) || answer.contains("2")) {
				// 현재 매칭 가능 회수는 몇회이며, 2일에 한번씩 1회가 충전된다는 식의 알람 메세지가 필요함.
				// 처음엔 2로 else if문으로 들어와서 매칭 가능 회수 차감 질문으로 들어간다
				// 그 다음 "네! 하겠습니"를 선택하면 밑의 if문으로 들어가지 못하도록 하는 방식임.
				
				
				///////////////////////////////////////////////////////////////////////////
				///////////////////접속 했을때 시간 비교해서 매칭 가능 1회 증가 시켜주기 //////////////////////
				///////////////////////////////////////////////////////////////////////////
				///월,수,금,토요일날 접속할때 1 증가 시켜준다.
				
				TimeZone jst = TimeZone.getTimeZone("JST");
				// 주어진 시간대에 맞게 현재 시각으로 초기화된 GregorianCalender 객체를 반환.
				Calendar cal = Calendar.getInstance(jst);
				int current_month = cal.get(Calendar.MONTH) + 1;
				int current_date = cal.get(Calendar.DATE);
				//int current_hour = cal.get(Calendar.HOUR_OF_DAY);
				int day_of_week = cal.get ( Calendar.DAY_OF_WEEK );

				String current_time_stamp_str = "" + current_month + current_date;
				int current_time = Integer.parseInt(current_time_stamp_str);
				String available_check = "";

				switch(day_of_week) {
				case 2:
				case 4:
				case 5:
				case 7:
					// 월,수,금,토(일요일부터 1)
					int get_time_stamp = id_item.getInt("time_stamp");
					if (get_time_stamp != current_time) {
						// 매치 가능 회수 1회 증가 후 증가 했을때의 시간을 입력해 넣는다.
						if(available == MATCH_LIMIT) {
							available = MATCH_LIMIT;
							available_check = "이미 매칭 가능 회수가 최대이므로 매칭 회수가 추가 되지 않습니다!\n";
						}else {
							available++;
							available_check = "매칭 가능 회수가 1회 추가 되었습니다!\n";

						}
						//매칭 회수 추가 되었을 때의 시간 기록, 매칭 회수 1회 증가 UPDATE
						table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
								new AttributeUpdate("time_stamp").put(current_time),
								new AttributeUpdate("available").put(available)));
					}

				}
				

				//////////////////////////////////////////////////////////////////////////////
				///////////////////////////접속시간비교하는곳///////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////

				if (!answer.contains(ASSURE_MSG)) {
					
					if (available == 0)
						return main_message("매칭 가능 회수가 부족해요!\n다음날 충전 시 까지 기다려 주세요!");
					
					jsAns.put("text", available_check + "현재 매칭 가능 회수는  " + available + "회 이며, 매칭시 1회가 차감됩니다."
							+ "\n새로운 이성을 찾으시겠습니까?(월, 수, 금, 토요일에 접속시 매칭 가능 회수가 1회 증가 됩니다.)");
					
					jsMes.put("message", jsAns);
					JSONObject key2 = new JSONObject();
					JSONObject key3 = new JSONObject();
					jsArr.add(ASSURE_MSG);
					jsArr.add("나중에 할게요!");
					key2.put("type", "buttons");
					key2.put("buttons", jsArr);
					key3.put("keyboard", key2);
					jsMes.putAll(key3);
					return jsMes;
				}
				

				// 매칭 한번 더 하기
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////// MATCHSTART/////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////

				int get_sex = id_item.getInt("sex");
				String w_age = id_item.getString("w_age");
				String w_height = id_item.getString("w_height");
				String my_ORD = id_item.getString("ORD");

				int w_age_i_upper = Integer.parseInt(w_age) + 3;
				String w_age_up = String.valueOf(w_age_i_upper);

				int w_height_i = Integer.parseInt(w_height);
				w_height_i -= 4;
				w_height = String.valueOf(w_height_i);

				String op_sex;
				if (get_sex == 1) {
					op_sex = "0";
				} else
					op_sex = "1";

				Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
				expressionAttributeValues.put(":match", new AttributeValue().withN("1"));
				expressionAttributeValues.put(":sex", new AttributeValue().withN(op_sex));
				expressionAttributeValues.put(":wish_height", new AttributeValue().withS(w_height));
				expressionAttributeValues.put(":wish_age_lo", new AttributeValue().withS(w_age));
				expressionAttributeValues.put(":wish_age_up", new AttributeValue().withS(w_age_up));

				ScanRequest scanRequest = new ScanRequest().withTableName(TABLE).withFilterExpression(
						"is_match = :match and sex = :sex and height >= :wish_height and age >= :wish_age_lo and age <= :wish_age_up")
						.withProjectionExpression("id, ORD").withExpressionAttributeValues(expressionAttributeValues);
				ScanResult result = client.scan(scanRequest);

				Vector Ops = new Vector();
				Set<String> histories = id_item.getStringSet("history");

				Set<String> matched = null;
				for (Map<String, AttributeValue> item : result.getItems()) {
					String compare_ORD = "";
					Op_Info op = new Op_Info();

					// SELECT된 사람의 ORD(성향) 정보를 받는다.
					String get_ORD = item.get("ORD").getS();

					// SELECT된 사람의 id를 받는다.
					op.op_id = item.get("id").getS();

					if (histories.contains(op.op_id))
						continue;

					for (int i = 0; i < get_ORD.length(); i++) {

						// 상대방의 알파벳이 나의 취향 알파벳 어디에 해당하는지 indexOf로 받고 compare_ORD에 그 결과값을 하나씩 더함
						char get_char = get_ORD.charAt(i);
						compare_ORD += my_ORD.indexOf(get_char);

					}
					op.op_ORD = Integer.parseInt(compare_ORD);
					Ops.add(op);

				}

				////////////// 매칭에 맞는 사람이 한명도 없을때////////////////////////
				///////////////////////////////////////////////////////////

				if (result.getItems().isEmpty() || Ops.size() <= 1) {
					table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key)
							.withAttributeUpdate(new AttributeUpdate("state").put(STATE_CHG_COND_RDY)));
					jsAns.put("text", "현재 매칭에 맞는 분이 없는 것 같아요.. 조건을 바꿔 보시겠어요?");
					jsMes.put("message", jsAns);
					JSONObject key = new JSONObject();
					JSONObject key1 = new JSONObject();
					jsArr.add("네! 바꾸겠습니다!");
					jsArr.add("아니오! 기다리겠습니다!");
					key.put("type", "buttons");
					key.put("buttons", jsArr);
					key1.put("keyboard", key);
					jsMes.putAll(key1);
					return jsMes;
				}

				/////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////// SORTING///////////////////////////////////////

				Collections.sort(Ops, new MemberComparator());

				////////////////////////////////////// 중복방지////////////////////////////////////////
				///////////////////////////////////// UPDATE////////////////////////////////////////
				histories.add(((Op_Info) Ops.get(0)).op_id);
				histories.add(((Op_Info) Ops.get(1)).op_id);

				// 매칭 된 사람을 본인의 history에 추가하고, 매칭 가능 회수 1회 줄이는 UPDATE
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("history").put(histories),
						new AttributeUpdate("available").put(available - 1))

				);

				Map<String, String> expressionAttributeNames = new HashMap<String, String>();
				expressionAttributeNames.put("#M", "matched");

				Map<String, Object> ev = new HashMap<String, Object>();
				ev.put(":matched_ppl",
						new HashSet<String>(Arrays.asList(((Op_Info) Ops.get(0)).op_id, ((Op_Info) Ops.get(1)).op_id)));

				UpdateItemOutcome outcome = table.updateItem("id", // key attribute name
						user_key, // key attribute value
						"set #M = :matched_ppl", // UpdateExpression
						expressionAttributeNames, ev);
				////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////// UPDATE//END//////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////

				// 결과 보여주는것
				return main_message("매칭에 성공 하였습니다!!\n\"1.매칭 결과 확인하기\" 메뉴를 눌러 결과를 확인 해 보세요!");

				//////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////
				///////////////////////////// MATCH END 2번 눌렸을때 끝///////////////////////////////
				///////////////////////////////////////////////////////////////////////////////////
				///////////////////////////////////////////////////////////////////////////////////

			} else if (answer.contains("3")) {
				// 사용법

				return main_message("매칭은 한번에 총 두분이 소개되며, 한번 매칭된 두 분은 다시 소개 되지 않습니다.\n"
						+ "현재 회원님의 매칭 가능 회수는 " +available +"회 이며, 매주 월요일, 수요일, 금요일, 토요일 접속시 1회가 추가 됩니다.\n"
								+ "매칭 가능 회수는 최대 "+ MATCH_LIMIT +"회 이며, 그 이상은 추가되지 않습니다.");
			} else if (answer.contains("4")) {
				// 공지사항

				return main_message("공지사항은 ㅓㄹ이ㅏ멍ㄹㄴㄻㅇ너ㅏㅣㅏ;ㅓㄹㅇㅇㄴㄹ;ㅓㅏㅣ라어;ㅣㄹ아ㅓ라ㅓㅣ;ㅁㄴㄹㅇ;ㅓㅏㄴㅁㄹㅇㄴㄹ어;ㄴ러ㅏㅇ마ㅓㄴㄹㅇ"
						+ "ㅇㄹ너ㅏㄹㅇ나ㅓㅁ;ㄻㄴㅇ라ㅓㅣ런ㅇㅁ;ㅓㅏㅣㅁㄹ앙러ㅣㅓㄹㅇ마ㅓㅇ란ㅇ란ㅁ아ㅓ님;ㅇ러닝런미ㅏ아ㅓㅣㄹㄴ언ㅇ마ㅣ러ㅓㅏㄹㄴㅇ미ㅓㄴ"
						+ "ㅏㅓㅣㄹㄴㅇㅁㄴ이러ㅏㅣㅓㅏㅇㄻ;니러알어ㅏㅏ너리ㅓㄴ일");
			}else
				return main_message("메인 화면으로 돌아갑니다!");

		}
		
		
		case STATE_INIT_HEIGHT: {
			// 키 입력 후
			try {
				Integer.parseInt(answer);
			} catch (NumberFormatException e) {
				jsAns.put("text", "죄송합니다! 키는 숫자만 입력 해 주세요ㅜㅜ");
				jsMes.put("message", jsAns);
				return jsMes;
			}
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("height").put(answer), new AttributeUpdate("state").put(STATE_INIT_AGE)));
			jsAns.put("text", "실례지만 나이가 어떻게 되세요?(숫자만 입력 해 주세요)");
			jsMes.put("message", jsAns);
			return jsMes;
		}
		case STATE_INIT_AGE: {
			try {
				Integer.parseInt(answer);
			} catch (NumberFormatException e) {
				jsAns.put("text", "죄송합니다! 나이는 숫자만 입력 해 주세요ㅜㅜ");
				jsMes.put("message", jsAns);
				return jsMes;
			}
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("age").put(answer), new AttributeUpdate("state").put(STATE_INIT_WISH_HEIGHT)));
			jsAns.put("text", "원하시는 이성의 키가 어떻게 되나요?");
			jsMes.put("message", jsAns);
			return jsMes;
		}
		case STATE_INIT_WISH_HEIGHT: {
			try {
				Integer.parseInt(answer);
			} catch (NumberFormatException e) {
				jsAns.put("text", "죄송합니다! 키는 숫자만 입력 해 주세요ㅜㅜ");
				jsMes.put("message", jsAns);
				return jsMes;
			}
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("w_height").put(answer),
					new AttributeUpdate("state").put(STATE_INIT_WISH_AGE)));
			jsAns.put("text", "원하시는 이성의 나이가 어떻게 되나요?");
			jsMes.put("message", jsAns);
			return jsMes;
		}
		case STATE_INIT_WISH_AGE: {
			try {
				Integer.parseInt(answer);
			} catch (NumberFormatException e) {
				jsAns.put("text", "죄송합니다! 나이는 숫자만 입력 해 주세요ㅜㅜ");
				jsMes.put("message", jsAns);
				return jsMes;
			}
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("w_age").put(answer), new AttributeUpdate("state").put(STATE_INIT_SEX)));
			jsAns.put("text", "이용하시는 분의 성별이 어떻게 되세요?");
			jsMes.put("message", jsAns);
			JSONObject key = new JSONObject();
			JSONObject key1 = new JSONObject();
			jsArr.add("여성");
			jsArr.add("남성");
			key.put("type", "buttons");
			key.put("buttons", jsArr);
			key1.put("keyboard", key);
			jsMes.putAll(key1);
			return jsMes;
		}
		case STATE_INIT_SEX: {

			int sex_answer;
			if (answer.contains("남")) {
				sex_answer = 1;
			} else
				sex_answer = 0;

			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("sex").put(sex_answer), new AttributeUpdate("state").put(STATE_NORMAL)));

			return main_message(FINAL_MSG);
			
		}
		case STATE_INIT: {
			// 닉네임 입력 후,
			////////////////////////////////////////ERRORCHECK///////////////////////////////////////////////////////
			if(answer.contains(IDLE)) {
				//서버 timeout오류 메세지로 온 경우 에러 리턴
				jsAns.put("text", "알럽고가 생각할 시간이 필요한가봐요.. 죄송하지만 다시 닉네임을 입력 해 주시겠어요?");
				jsMes.put("message", jsAns);
				return jsMes;
			}
			////////////////////////////////////////ERRORCHECK///////////////////////////////////////////////////////
			/////////////////////////////////////////이전으로 돌아온 에러 체크////////////////////////////////////////////////

			if (!answer.contains(IDLE_BUTTON)) {
				//일반적인 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("nick").put(answer),
						new AttributeUpdate("state").put(STATE_Q_1)));
			}else {
				//다음 질문 오류 때문에 다시 돌아왔을때
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_Q_1)
						));
			}
			/////////////////////////////////////////이전으로 돌아온 에러 체크////////////////////////////////////////////////
			
			Q = "**여러분의 연애 성향을 확인하기 위해 총 24개의 문항들이 준비되어 있습니다! 꾸밈없이 솔직히 답해 주셔야 여러분에게 꼭 맞는 이상형이 소개 될 수 있습니다!**\n"
					+ "(1/24) 사랑하는 사람이 중요한 기념일을 잊어버린 듯 하다.. 당신의 태도는?";
			A = "A. 나의 분노를 여지없이 보여준다.";
			B = "B. 기분은 나빠도 밝게 분위기를 띄운다.";
			C = "C. 그럴 수 있다. 쿨하게 넘어간다.";
			D = "D. 다른 의도가 있는지 곰곰히 생각한다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return question_message(url, Q, A, B, C, D);
			

		}
		case STATE_Q_1: {

			Q = "(2/24) 사랑하는 그대가  갑자기 자기 이전에 몇 명을 사귀었냐고 묻는다...왜? 나의 태도는?";
			A = "A. 솔직하게 말한다.";
			B = "B. 무조건 당신이 제일 중요하다고 이야기 한다.";
			C = "C. 기분이 나쁘지 않게 두리뭉실 말한다.";
			D = "D. 좋아할만한 대답을 해준다.";
			url = "https://steptohealth.co.kr/wp-content/uploads/2018/03/4-copule.jpg";
			return QUESTIONS(STATE_Q_1, Q, A, B, C, D, url, answer, user_key, table);

		}
		case STATE_Q_2: {
			// 11
			Q = "(3/24) 난 내 스타일이 있는데! 연인이 내가 입은 옷에 대해 지적을 한다면 당신의 태도는?";
			A = "A. 내 스타일에 신경 쓰지 말아달라 부탁한다.";
			B = "B. 내 스타일의 매력을 어필해 설득한다.";
			C = "C. 알겠다 말하고 바꾸지는 않는다.";
			D = "D. 싫지만 상대가 원하는 옷을 입어준다.";
			url = "http://talkimg.imbc.com/TVianUpload//tvian/image/2015/06/17/4KtbZ0gR2Jhe635701480639246586.jpg";
			return QUESTIONS(STATE_Q_2, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_3: {
			// 11
			Q = "(4/24) 그 음식 너무 싫은데..! 연인이 내가 싫어하는 음식을 먹자고 조른다면?";
			A = "A. 싫어! 절대 네버 먹지 않는다.";
			B = "B. 사랑하는 사람을 위해서면 즐겁게 먹는다.";
			C = "C. 다음에 먹자고 말하고 안먹는다.";
			D = "D. 싫은 이유에 대해 논리적으로 설명한다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_3, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_4: {
			// 11
			Q = "(5/24) 연인이 ";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_4, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_5: {
			// 11
			Q = "(6/24) 급한 시험/과제 전 날 갑자기 연인이 보고싶다고 조르면?";
			A = "A. 연인은 그 다음날 봐도 된다.급한게 먼저다.";
			B = "B. 당연히 본다.내겐 연인이 더 중요하다.";
			C = "C. 공부와 연애의 타협점을 찾는다.";
			D = "D. 날 이해 해 주지 못하는 연인에 화가난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_6: {
			// 11
			Q = "(7/24) 당신의 데이트 코스는?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_6, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_7: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_8: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_9: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_10: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_11: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_12: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_13: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_14: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_15: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_16: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_17: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_18: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_19: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_20: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_21: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_22: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_23: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_24: {
			// 11
			Q = "(6/24)~~ 할땐 어떻게 하시나요?";
			A = "A. 대담하게 진도를 나간다 .";
			B = "B. 힘들어도 활기를 잃지 않는다.";
			C = "C. 싫어도 기꺼이 한다.";
			D = "D. 항상 계획을 짜고 만난다.";
			url = "http://postfiles10.naver.net/20160413_137/dh85_1460550837347Y6DsP_JPEG/20160324_111115.jpg?type=w773";
			return QUESTIONS(STATE_Q_5, Q, A, B, C, D, url, answer, user_key, table);
		}
		case STATE_Q_LAST: {
			//40
			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////
			if(answer.contains(IDLE)) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_Q_LAST - 1)));
				return fail_message();
			}
			
			if (!answer.contains(IDLE_BUTTON)) {
				//일반적인 흐름
				String answer_table;
				if(answer.contains("A")) {
					answer_table = "A";
				}else if(answer.contains("B")) {
					answer_table = "B";

				}else if(answer.contains("C")) {
					answer_table = "C";
				}else {
					answer_table = "D";
				}
				
			}
				
				
			//ORD ABCD 순서 정하는 곳
			List my_ord = new ArrayList();
			int a = id_item.getInt("A");
			int b = id_item.getInt("B");
			int c = id_item.getInt("C");
			int d = id_item.getInt("D");
			
			my_ord.add(new my_ord("A",a));
			my_ord.add(new my_ord("B",b));
			my_ord.add(new my_ord("C",c));
			my_ord.add(new my_ord("D",d));
			
			Collections.sort(my_ord, new MemberComparator1());
			
			String set_ORD = null;
			for (int i = 0; i < my_ord.size(); i++) {
				my_ord get = new my_ord();
				get = (my_ord) my_ord.get(i);
				set_ORD += get.ORD_char;
			}

			
			if (!answer.contains(IDLE_BUTTON)) {
				// 일반적인 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("ORD").put(answer),
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_1)));
			} else {
				// 다음 질문에서 에러나서 다시 돌아온 경우
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key)
						.withAttributeUpdate(new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_1)));
			}
			///////////////////////////////////////// ERROR
			///////////////////////////////////////// CHECK/////////////////////////////////////////////////////////////

			jsAns.put("text", "이성의  1순위 성향을 선택 해 주세요! A는 ~~형으로 ~~하는 성격을 갖고 있으며, B는 ~~형으로 ~~할때 ~~하는"
					+ "성격을 갖고 있습니다. C는 ~~형으로 ~~할때 ~~하는 성격을 가지고 있고, 마지막으로 D는 ~~할때 ~~를 주로 ~~하는 성향을 보이는 분 입니다.");
			jsMes.put("message", jsAns);
			JSONObject key = new JSONObject();
			JSONObject key1 = new JSONObject();
			jsArr.add("A");
			jsArr.add("B");
			jsArr.add("C");
			jsArr.add("D");
			key.put("type", "buttons");
			key.put("buttons", jsArr);
			key1.put("keyboard", key);
			jsMes.putAll(key1);
			return jsMes;

		}
		case STATE_INIT_WISH_ORD_1: {	
		//41
			
			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////
			if( answer.contains(IDLE) ) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_Q_LAST)));
				return fail_message();
			}
			
			
			if(!answer.contains(IDLE_BUTTON)) {
				//정상적인 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("w_ORD").put(answer),
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_2)
						));
			}else {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_2)
						));
			}
			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////

			jsAns.put("text", "이성의 2순위 성향을 선택 해 주세요! A는 ~~형으로 ~~하는 성격을 갖고 있으며, B는 ~~형으로 ~~할때 ~~하는"
					+ "성격을 갖고 있습니다. C는 ~~형으로 ~~할때 ~~하는 성격을 가지고 있고, 마지막으로 D는 ~~할때 ~~를 주로 ~~하는 성향을 보이는 분 입니다.");

			String[] temp = { "A", "B", "C", "D" };
			jsMes.put("message", jsAns);
			JSONObject key = new JSONObject();
			JSONObject key1 = new JSONObject();
			for (String alp : temp) {
				if (!answer.contains(alp))
					jsArr.add(alp);
			}
			key.put("type", "buttons");
			key.put("buttons", jsArr);
			key1.put("keyboard", key);
			jsMes.putAll(key1);
			return jsMes;

		}
		case STATE_INIT_WISH_ORD_2: {
			//42
			String ord_result = "";
			String ORD_1 = id_item.getString("w_ORD");
			ord_result += ORD_1;
			ord_result += answer;

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////
			if( answer.contains(IDLE)) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_1)));
				return fail_message();
			}
			
			
			if(!answer.contains(IDLE_BUTTON)) {
				//정상적인 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("w_ORD").put(ord_result),
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_3)
						));
			}else {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_3)
						));
			}

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////

			jsAns.put("text", "이성의 3순위 성향을 선택 해 주세요! A는 ~~형으로 ~~하는 성격을 갖고 있으며, B는 ~~형으로 ~~할때 ~~하는"
					+ "성격을 갖고 있습니다. C는 ~~형으로 ~~할때 ~~하는 성격을 가지고 있고, 마지막으로 D는 ~~할때 ~~를 주로 ~~하는 성향을 보이는 분 입니다.");

			String[] temp = { "A", "B", "C", "D" };
			jsMes.put("message", jsAns);
			JSONObject key = new JSONObject();
			JSONObject key1 = new JSONObject();
			for (String alp : temp) {
				if (!ord_result.contains(alp))
					jsArr.add(alp);
			}
			key.put("type", "buttons");
			key.put("buttons", jsArr);
			key1.put("keyboard", key);
			jsMes.putAll(key1);
			return jsMes;
		}
		case STATE_INIT_WISH_ORD_3: {
			//43
			String ord_result = "";
			String ORD_2 = id_item.getString("w_ORD");
			ord_result += ORD_2;
			ord_result += answer;

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////
			if( answer.contains(IDLE) ) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_2)));
				return fail_message();
			}
			
			
			if(!answer.contains(IDLE_BUTTON)) {
				//정상적인 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("w_ORD").put(ord_result),
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_4)
						));
			}else {
				//다음 질문 오류나서 돌아온 흐름
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_4)
						));
			}

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////

			jsAns.put("text", "이성의 4순위 성향을 선택 해 주세요! A는 ~~형으로 ~~하는 성격을 갖고 있으며, B는 ~~형으로 ~~할때 ~~하는"
					+ "성격을 갖고 있습니다. C는 ~~형으로 ~~할때 ~~하는 성격을 가지고 있고, 마지막으로 D는 ~~할때 ~~를 주로 ~~하는 성향을 보이는 분 입니다.");

			String[] temp = { "A", "B", "C", "D" };
			jsMes.put("message", jsAns);
			JSONObject key = new JSONObject();
			JSONObject key1 = new JSONObject();
			for (String alp : temp) {
				if (!ord_result.contains(alp))
					jsArr.add(alp);
			}
			key.put("type", "buttons");
			key.put("buttons", jsArr);
			key1.put("keyboard", key);
			jsMes.putAll(key1);
			return jsMes;
		}
		case STATE_INIT_WISH_ORD_4: {
			//44
			// 닉네임 -> ""질문아직구현안함"" -> 상대ORD(현재위치) ->(다음질문)본인키 -> 본인나이 -> 이성나이 -> 이성키 ->
			String ord_result = "";
			String ORD_2 = id_item.getString("w_ORD");
			ord_result += ORD_2;
			ord_result += answer;

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////
			if(answer.contains(IDLE) && !answer.contains(IDLE_BUTTON)) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_WISH_ORD_3)));
				return fail_message();
			}
						
			if(!answer.contains(IDLE_BUTTON)) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("w_ORD").put(ord_result),
						new AttributeUpdate("state").put(STATE_INIT_HEIGHT)));
			}else {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
						new AttributeUpdate("state").put(STATE_INIT_HEIGHT)));
			}

			/////////////////////////////////////////ERROR CHECK/////////////////////////////////////////////////////////////


			jsAns.put("text", "실례지만 키가 어떻게 되세요?(숫자만 입력 해 주세요!)");
			jsMes.put("message", jsAns);
			return jsMes;
		}

		case STATE_CHG_COND_RDY: {
			if (answer.contains("네")) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key)
						.withAttributeUpdate(new AttributeUpdate("state").put(STATE_INIT_WISH_HEIGHT)));
				jsAns.put("text", "원하시는 이성의 키가 어떻게 되나요?(숫자만 입력해 주세요!)");
				jsMes.put("message", jsAns);
				return jsMes;
			} else if (answer.contains("아")) {
				table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key)
						.withAttributeUpdate(new AttributeUpdate("state").put(STATE_NORMAL)));
				return main_message("메인 화면으로 돌아갑니다!");
			}
		}

		default: {
			return main_message("메인 화면으로 돌아갑니다!");
		}

		}	

	}

	private JSONObject QUESTIONS(final int STATE_Q, String Q, String A, String B, String C, String D, String url,
			String answer, String user_key, Table table) {
		////////////////////////////////////////ERRORCHECK///////////////////////////////////////////////////////
		if(answer.contains(IDLE)) {
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("state").put(STATE_Q - 1)));
			return fail_message();
		}
		////////////////////////////////////////ERRORCHECK///////////////////////////////////////////////////////
		/////////////////////////////////////////이전으로 돌아온 에러 체크////////////////////////////////////////////////

		if (!answer.contains(IDLE_BUTTON)) {
			//일반적인 흐름
			String answer_table;
			if(answer.contains("A")) {
				answer_table = "A";
			}else if(answer.contains("B")) {
				answer_table = "B";

			}else if(answer.contains("C")) {
				answer_table = "C";
			}else {
				answer_table = "D";
			}
			
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate(answer_table).addNumeric(1),
					new AttributeUpdate("state").put(STATE_Q + 1)
					));
		}else {
			//다음 질문 오류 때문에 다시 돌아왔을때
			table.updateItem(new UpdateItemSpec().withPrimaryKey("id", user_key).withAttributeUpdate(
					new AttributeUpdate("state").put(STATE_Q + 1)
					));
		}
		JSONObject main_jsAns = new JSONObject();
		JSONObject main_jsMes = new JSONObject();
		JSONArray main_jsArr = new JSONArray();
		JSONObject main_pic = new JSONObject();
		JSONObject main_pic_wrapper = new JSONObject();
		
		main_jsAns.put("text", Q);
		main_pic.put("url",url);
		main_pic.put("width", 640);
		main_pic.put("height", 480);
		main_pic_wrapper.put("photo", main_pic);
		main_jsAns.putAll(main_pic_wrapper);
		main_jsMes.put("message", main_jsAns);
		JSONObject key = new JSONObject();
		JSONObject key1 = new JSONObject();
		main_jsArr.add(A);
		main_jsArr.add(B);
		main_jsArr.add(C);
		main_jsArr.add(D);
		key.put("type", "buttons");
		key.put("buttons", main_jsArr);
		key1.put("keyboard", key);
		main_jsMes.putAll(key1);
		return main_jsMes;
	}

	private JSONObject main_message(String txt) {
		JSONObject main_jsAns = new JSONObject();
		JSONObject main_jsMes = new JSONObject();
		JSONArray main_jsArr = new JSONArray();
		main_jsAns.put("text", txt);
		main_jsMes.put("message", main_jsAns);
		JSONObject key = new JSONObject();
		JSONObject key1 = new JSONObject();
		main_jsArr.add("1.매칭 결과 확인하기");
		main_jsArr.add("2.매칭 시작하기");
		main_jsArr.add("3.알럽고 사용 방법");
		main_jsArr.add("4.공지사항");
		key.put("type", "buttons");
		key.put("buttons", main_jsArr);
		key1.put("keyboard", key);
		main_jsMes.putAll(key1);
		return main_jsMes;
	}
	private JSONObject question_message(String url, String txt,String A, String B, String C, String D) {
		JSONObject main_jsAns = new JSONObject();
		JSONObject main_jsMes = new JSONObject();
		JSONArray main_jsArr = new JSONArray();
		JSONObject main_pic = new JSONObject();
		JSONObject main_pic_wrapper = new JSONObject();
		main_jsAns.put("text", txt);
		main_pic.put("url",url);
		main_pic.put("width", 640);
		main_pic.put("height", 480);
		main_pic_wrapper.put("photo", main_pic);
		main_jsAns.putAll(main_pic_wrapper);
		main_jsMes.put("message", main_jsAns);
		JSONObject key = new JSONObject();
		JSONObject key1 = new JSONObject();
		main_jsArr.add(A);
		main_jsArr.add(B);
		main_jsArr.add(C);
		main_jsArr.add(D);
		key.put("type", "buttons");
		key.put("buttons", main_jsArr);
		key1.put("keyboard", key);
		main_jsMes.putAll(key1);
		return main_jsMes;
	}

	
	private JSONObject fail_message() {
		JSONObject main_jsAns = new JSONObject();
		JSONObject main_jsMes = new JSONObject();
		JSONArray main_jsArr = new JSONArray();
		main_jsAns.put("text", FAIL_MSG);
		main_jsMes.put("message", main_jsAns);
		JSONObject key = new JSONObject();
		JSONObject key1 = new JSONObject();
		main_jsArr.add(IDLE_BUTTON);
		key.put("type", "buttons");
		key.put("buttons", main_jsArr);
		key1.put("keyboard", key);
		main_jsMes.putAll(key1);
		return main_jsMes;
	}

	class Op_Info {
		public String op_id;
		public int op_ORD;
	}
	class my_ord{
		public String ORD_char;
		public int ORD_count;
		public my_ord() {
		}
		public my_ord(String _char,int _count) {
			this.ORD_char = _char; this.ORD_count = _count;
		}
	}

	class MemberComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			return Integer.compare(((Op_Info) arg0).op_ORD, ((Op_Info) arg1).op_ORD);
		}
	}
	class MemberComparator1 implements Comparator {
		public int compare(Object arg0, Object arg1) {
			return -Integer.compare(((Op_Info) arg0).op_ORD, ((Op_Info) arg1).op_ORD);
		}
	}

}
