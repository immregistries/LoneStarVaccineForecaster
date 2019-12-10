package org.immregistries.lonestar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FhirTestServlet extends FhirServlet {

	public static final String JSON_EXAMPLE = "{\r\n" + "  \"resourceType\": \"Parameters\",\r\n"
			+ "  \"id\": \"parameters-in-example\",\r\n" + "  \"parameter\": [\r\n" + "    {\r\n"
			+ "      \"name\": \"assessmentDate\",\r\n" + "      \"valueDate\": \"2019-06-27\"\r\n" + "    },\r\n"
			+ "    {\r\n" + "      \"name\": \"patient\",\r\n" + "      \"resource\": {\r\n"
			+ "        \"resourceType\": \"Patient\",\r\n" + "        \"id\": \"forecast-example\",\r\n"
			+ "        \"meta\" : {\r\n" + "          \"profile\" : [\r\n"
			+ "            \"http://hl7.org/fhir/uv/immds/StructureDefinition/immds-patient\"\r\n" + "          ]\r\n"
			+ "        },\r\n" + "        \"identifier\" : [\r\n" + "          {\r\n"
			+ "            \"_system\" : {\r\n" + "              \"extension\" : [\r\n" + "                {\r\n"
			+ "                  \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                  \"valueCode\" : \"masked\"\r\n" + "                }\r\n" + "              ]\r\n"
			+ "            },\r\n" + "            \"_value\" : {\r\n" + "              \"extension\" : [\r\n"
			+ "                {\r\n"
			+ "                  \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                  \"valueCode\" : \"masked\"\r\n" + "                }\r\n" + "              ]\r\n"
			+ "            }\r\n" + "          }\r\n" + "        ],\r\n" + "        \"name\" : [\r\n"
			+ "          {\r\n" + "            \"_family\" : {\r\n" + "              \"extension\" : [\r\n"
			+ "                {\r\n"
			+ "                  \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                  \"valueCode\" : \"masked\"\r\n" + "                }\r\n" + "              ]\r\n"
			+ "            },\r\n" + "            \"_given\" : [\r\n" + "              {\r\n"
			+ "                \"extension\" : [\r\n" + "                  {\r\n"
			+ "                    \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                    \"valueCode\" : \"masked\"\r\n" + "                  }\r\n" + "                ]\r\n"
			+ "              }\r\n" + "            ]\r\n" + "          }\r\n" + "        ],\r\n"
			+ "        \"gender\": \"male\",\r\n" + "        \"birthDate\": \"2019-04-28\"\r\n" + "      }\r\n"
			+ "    },\r\n" + "    {\r\n" + "      \"name\": \"immunization\",\r\n" + "      \"resource\": {\r\n"
			+ "        \"resourceType\": \"Immunization\",\r\n"
			+ "        \"id\": \"c9d3fd2e-cf34-44f8-aa68-4413a01c4153\",\r\n" + "        \"meta\": {\r\n"
			+ "          \"versionId\": \"1\",\r\n" + "          \"lastUpdated\": \"2019-06-27T11:55:25.382-04:00\"\r\n"
			+ "        },\r\n" + "        \"contained\": [\r\n" + "          {\r\n"
			+ "            \"resourceType\": \"Patient\",\r\n" + "            \"id\": \"patient-forecast-data\",\r\n"
			+ "            \"meta\" : {\r\n" + "              \"profile\" : [\r\n"
			+ "                \"http://hl7.org/fhir/uv/immds/StructureDefinition/immds-patient\"\r\n"
			+ "              ]\r\n" + "            },\r\n" + "            \"identifier\" : [\r\n"
			+ "              {\r\n" + "                \"_system\" : {\r\n" + "                  \"extension\" : [\r\n"
			+ "                    {\r\n"
			+ "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                      \"valueCode\" : \"masked\"\r\n" + "                    }\r\n"
			+ "                  ]\r\n" + "                },\r\n" + "                \"_value\" : {\r\n"
			+ "                  \"extension\" : [\r\n" + "                    {\r\n"
			+ "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                      \"valueCode\" : \"masked\"\r\n" + "                    }\r\n"
			+ "                  ]\r\n" + "                }\r\n" + "              }\r\n" + "            ],\r\n"
			+ "            \"name\" : [\r\n" + "              {\r\n" + "                \"_family\" : {\r\n"
			+ "                  \"extension\" : [\r\n" + "                    {\r\n"
			+ "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                      \"valueCode\" : \"masked\"\r\n" + "                    }\r\n"
			+ "                  ]\r\n" + "                },\r\n" + "                \"_given\" : [\r\n"
			+ "                  {\r\n" + "                    \"extension\" : [\r\n" + "                      {\r\n"
			+ "                        \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",\r\n"
			+ "                        \"valueCode\" : \"masked\"\r\n" + "                      }\r\n"
			+ "                    ]\r\n" + "                  }\r\n" + "                ]\r\n" + "              }\r\n"
			+ "            ],\r\n" + "            \"gender\": \"male\",\r\n"
			+ "            \"birthDate\": \"2019-04-28\"\r\n" + "          }\r\n" + "        ],\r\n"
			+ "        \"status\": \"completed\",\r\n" + "        \"vaccineCode\": {\r\n"
			+ "          \"coding\": [\r\n" + "            {\r\n"
			+ "              \"system\": \"http://hl7.org/fhir/sid/cvx\",\r\n" + "              \"code\": \"08\",\r\n"
			+ "              \"display\": \"Hep B, adolescent or pediatric\"\r\n" + "            }\r\n"
			+ "          ]\r\n" + "        },\r\n" + "        \"patient\": {\r\n"
			+ "          \"reference\": \"#patient-forecast-data\"\r\n" + "        },\r\n"
			+ "        \"occurrenceDateTime\": \"2019-04-29\",\r\n"
			+ "        \"recorded\": \"2019-04-29T00:00:00-04:00\",\r\n" + "        \"primarySource\": false\r\n"
			+ "      }\r\n" + "    }\r\n" + "  ]\r\n" + "}";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		PrintWriter out = new PrintWriter(resp.getOutputStream());
		out.println("<html><head><title>Lone Star Vaccine Forecaster FHIR Server</title></head><body>");
		out.println("<h1>FHIR Server</h1>");
		out.println(
				"<p>This is the end point for the DSTU2 FHIR end-point. This is experimental and may change at any time. </p>");
		out.println("<form method=\"POST\" action=\"fhirTest\">");
		out.println("  <textarea name=\"content\" cols=\"80\" rows=\"30\">" + JSON_EXAMPLE + "</textarea><br/>");
		out.println("  <input type=\"submit\" name=\"submit\" value=\"submit\"/>");
		out.println("</form>");
		out.println("</body></html>");
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String content = req.getParameter("content");
		InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
		ForecastInput forecastInput = new ForecastInput();
		if (content.startsWith("{"))
		{
		  forecastInput.json = true;
		}
		try {
			if (forecastInput.json) {
				readRequestJSON(forecastInput, in);
			} else {
				readRequestXML(forecastInput, in);
			}
		} catch (Exception e) {
			throw new ServletException("Unable to read request: " + e.getMessage(), e);
		}
		respond(resp, forecastInput);
	}

}
