package files;
import static io.restassured.RestAssured.*;

import java.io.File;

import org.testng.Assert;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

public class JiraTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        RestAssured.baseURI="http://localhost:8080";

        SessionFilter session = new SessionFilter();

        String response = given().header("Content-Type", "application/json").body("{ \"username\": \"abhishek\", \"password\": \"abhishek\" }")
        .log().all().filter(session).when().post("/rest/auth/1/session").then().log().all().extract().response().asString();

        String expectedmsg = "Hi How are you sir?....";
        String addComment = given().pathParam("key", "10001").log().all().header("Content-Type", "application/json").body("{\n" +
                "    \"body\": \""+expectedmsg+"\",\n" +
                "    \"visibility\": {\n" +
                "        \"type\": \"role\",\n" +
                "        \"value\": \"Administrators\"\n" +
                "    }\n" +
                "}").filter(session).when().post("/rest/api/2/issue/{key}/comment").then().log().all().assertThat().statusCode(201).extract().response().asString();
        JsonPath js = new JsonPath(addComment);
        String commentId = js.getString("id");

        given().header("X-Atlassian-Token", "no-check").header("Content-Type", "multipart/form-data").filter(session).pathParam("key", "10001").multiPart("file", new File("jira.txt"))
        .when().post("/rest/api/2/issue/{key}/attachments").then().log().all().assertThat().statusCode(200);

        //Get Issue
        String issueDetails = given().filter(session).pathParam("key", "10001").queryParam("fields", "comment")
        .when().get("/rest/api/2/issue/{key}").then().log().all().extract().response().asString();
        System.out.println(issueDetails);
        JsonPath js1 = new JsonPath(issueDetails);
        int commentcount = js1.getInt("fields.comment.comments.size()");
        for(int i=0;i<commentcount;i++)
        {
            String commentIdissue = js1.get("fields.comment.comments["+i+"].id").toString();
            if(commentIdissue.equalsIgnoreCase(commentId)) {
                String actualmsg = js1.get("fields.comment.comments["+i+"].body").toString();
                System.out.println(actualmsg);
                Assert.assertEquals(actualmsg, expectedmsg);
            }
        }
    }

}
