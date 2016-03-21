package gov.nist.healthcare.ttt.smtp.testcases;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.mail.dsn.DispositionNotification;


public class MU2ReceiverTests {
	public static Logger log = Logger.getLogger("MU2ReceiverTests");

	public TestResult fetchMail(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		HashMap<String, String> result = tr.getTestRequestResponses();
		HashMap<String, String> bodyparts = tr.getAttachments();
		LinkedHashMap<String, String> buffer = new LinkedHashMap<String, String>();
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		int dsnFlag = 0;
		int headerFlag = 0;
		Store store;
		Properties props = new Properties();

		TestResult t = ti.tr;

		String id = t.getMessageId();
		String fetch = t.getFetchType();
		String type = t.getSearchType();
		String startTime = t.getStartTime();
		Duration duration = null;
		Duration timeout = null;
		final long  timeoutConstant = 65; // 1 hour 5 mins (or) 65 minutes to get back the failure MDN


		try {


			Properties prop = new Properties();
			String path = "./application.properties";
			FileInputStream file = new FileInputStream(path);
			prop.load(file);
			file.close();

			Session session = Session.getDefaultInstance(props, null);

			store = session.getStore("imap");

			if (fetch.equals("smtp")){
				store.connect(ti.tttSmtpAddress,993,"failure15@hit-testing2.nist.gov",prop.getProperty("ett.password"));
			}
			else if (fetch.equals("imap")) {
				store.connect(ti.sutSmtpAddress,143,ti.sutUserName,ti.sutPassword);
			}

			else if (fetch.equals("imap1")) {
				store.connect("hit-testing.nist.gov",143,prop.getProperty("dir.username"), prop.getProperty("dir.password"));
			}

			else {
				store = session.getStore("pop3");
				store.connect(ti.sutSmtpAddress,110,ti.sutUserName,ti.sutPassword);
			}
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);


			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message messages[] = inbox.search(unseenFlagTerm);

			if(type.equals("fail")){
				System.out.println("Search in-reply-to or Failure MDN");
				for (Message message : messages){
					Enumeration headers = message.getAllHeaders();
					while(headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
						String x = h.getValue();
						if (id.equals(x)){
							dsnFlag = 1;
							ZonedDateTime endTime = ZonedDateTime.now();
							Enumeration headers1 = message.getAllHeaders();
							while (headers1.hasMoreElements()) {
								Header h1 = (Header) headers1.nextElement();
								//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
								result.put("\n"+h1.getName(), h1.getValue()+"\n");
								duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
								result.put("\nElapsed Time", duration.toString().substring(3)+"\n");

							}
							Multipart multipart = (Multipart) message.getContent();
							for (int i = 0; i < multipart.getCount(); i++) {
								BodyPart bodyPart = multipart.getBodyPart(i);
								InputStream stream = bodyPart.getInputStream();

								byte[] targetArray = IOUtils.toByteArray(stream);
								System.out.println(new String(targetArray));
								int m = i+1;
								//	bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

							}
						}

						if (dsnFlag == 0){
							Object m =  message.getContent();
							if (message.getContent() instanceof Multipart){
								Multipart multipart = (Multipart) message.getContent();
								for (int i = 0; i < ((Multipart) m).getCount(); i++){
									BodyPart bodyPart = multipart.getBodyPart(i);
									if (!(bodyPart.isMimeType("text/*"))){
										Object d =   bodyPart.getContent();
										//d.getNotifications();
										if (d instanceof DispositionNotification){
											Enumeration headers2 = ((DispositionNotification) d).getNotifications().getAllHeaders();
											while (headers2.hasMoreElements()) {
												Header h1 = (Header) headers2.nextElement();
												buffer.put("\n"+h1.getName(), h1.getValue()+"\n");
											}
											System.out.println(buffer);
											if(buffer.containsValue(id+"\n") && buffer.containsValue("automatic-action/MDN-sent-automatically;failure"+"\n")){
												//	buffer.get("\n"+"Disposition").toLowerCase().contains("fail");
												ZonedDateTime endTime = ZonedDateTime.now();
												result.putAll(buffer);
												duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
												result.put("\nElapsed Time", duration.toString().substring(3)+"\n");

											}


										}

									}

								}

							}
						}
					}
				}
			}

			else if (type.equals("timeout")){
				System.out.println("Search in-reply-to on Timeout");
				for (Message message : messages){
					Enumeration headers = message.getAllHeaders();
					while(headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
						String x = h.getValue();
						if (id.equals(x)){
							ZonedDateTime endTime = ZonedDateTime.now();
							Enumeration headers1 = message.getAllHeaders();
							while (headers1.hasMoreElements()) {
								Header h1 = (Header) headers1.nextElement();
								//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
								result.put("\n"+h1.getName(), h1.getValue()+"\n");
								timeout = Duration.between(endTime, ZonedDateTime.parse(startTime));
								result.put("\nElapsed Time", timeout.toString().substring(3)+"\n");

							}
							Multipart multipart = (Multipart) message.getContent();
							for (int i = 0; i < multipart.getCount(); i++) {
								BodyPart bodyPart = multipart.getBodyPart(i);
								InputStream stream = bodyPart.getInputStream();

								byte[] targetArray = IOUtils.toByteArray(stream);
								System.out.println(new String(targetArray));
								int m = i+1;
								bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

							}
						}
					}

				}

			}

			else if (type.equals("either")) { 
				System.out.println("Search Original-Message-Id for Processed/Dispatched in DN with no X-Header");
				int j = 1;
				for (Message message : messages){
					Object m =  message.getContent();
					if (message.getContent() instanceof Multipart){
						Multipart multipart = (Multipart) message.getContent();
						for (int i = 0; i < ((Multipart) m).getCount(); i++){
							BodyPart bodyPart = multipart.getBodyPart(i);
							if (!(bodyPart.isMimeType("text/*"))){
								Object d =   bodyPart.getContent();
								//d.getNotifications();
								if (d instanceof DispositionNotification){
									Enumeration headers2 = ((DispositionNotification) d).getNotifications().getAllHeaders();
									while (headers2.hasMoreElements()) {
										Header h1 = (Header) headers2.nextElement();
										if (id.equals(h1.getValue())){
											Enumeration headers3 = ((DispositionNotification) d).getNotifications().getAllHeaders();
											while (headers3.hasMoreElements()) {
												Header h2 = (Header) headers3.nextElement();
												buffer.put("\n"+h2.getName()+" "+j, h2.getValue()+"\n");
												list.add(h2.getValue());
												list1.add(h2.getName());
												j++;
											}
										}
										
										/*else{
											message.setFlag(Flags.Flag.SEEN, false);
										}*/
									}

									System.out.println(list);

									if(list1.contains("X-DIRECT-FINAL-DESTINATION-DELIVERY")){
										headerFlag = 1;
										ZonedDateTime endTime = ZonedDateTime.now();
										result.putAll(buffer);
										duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
										result.put("\nElapsed Time", duration.toString().substring(3)+"\n");
									}
									
									else if (list.contains("automatic-action/MDN-sent-automatically;processed")){
										ZonedDateTime endTime = ZonedDateTime.now();
										result.putAll(buffer);
										duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
										result.put("\nElapsed Time", duration.toString().substring(3)+"\n");
									}


								}

							}

						}

					}
				}

			}

			else if (type.equals("dispatched")) {
				System.out.println("Search Original-Message-Id for Dispatched in DN");
				String s = "";
				for (Message message : messages){
					Object m =  message.getContent();
					if (message.getContent() instanceof Multipart){
						Multipart multipart = (Multipart) message.getContent();
						for (int i = 0; i < ((Multipart) m).getCount(); i++){
							BodyPart bodyPart = multipart.getBodyPart(i);
							if (!(bodyPart.isMimeType("text/*"))){
								Object d =   bodyPart.getContent();
								//d.getNotifications();
								if (d instanceof DispositionNotification){
									Enumeration headers2 = ((DispositionNotification) d).getNotifications().getAllHeaders();
									while (headers2.hasMoreElements()) {
										Header h1 = (Header) headers2.nextElement();
										buffer.put("\n"+h1.getName(), h1.getValue()+"\n");
									}
									System.out.println(buffer);
									if(buffer.containsValue(id+"\n") && buffer.containsValue("automatic-action/MDN-sent-automatically;dispatched"+"\n")){
										ZonedDateTime endTime = ZonedDateTime.now();
										result.putAll(buffer);
										duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
										result.put("\nElapsed Time", duration.toString().substring(3)+"\n");

									}


								}

							}

						}

					}
				}

			}

			else if (type.equals("both")) {
				System.out.println("Search Original-Message-Id for Processed and Dispatched in DN");
				int j = 1;
				for (Message message : messages){
					Object m =  message.getContent();
					if (message.getContent() instanceof Multipart){
						Multipart multipart = (Multipart) message.getContent();
						for (int i = 0; i < ((Multipart) m).getCount(); i++){
							BodyPart bodyPart = multipart.getBodyPart(i);
							if (!(bodyPart.isMimeType("text/*"))){
								Object d =   bodyPart.getContent();
								//d.getNotifications();
								if (d instanceof DispositionNotification){
									Enumeration headers2 = ((DispositionNotification) d).getNotifications().getAllHeaders();
									while (headers2.hasMoreElements()) {
										Header h1 = (Header) headers2.nextElement();
										if (id.equals(h1.getValue())){
											Enumeration headers3 = ((DispositionNotification) d).getNotifications().getAllHeaders();
											while (headers3.hasMoreElements()) {
												Header h2 = (Header) headers3.nextElement();
												buffer.put("\n"+h2.getName()+" "+j, h2.getValue()+"\n");
												list.add(h2.getValue());
												j++;
											}
										}
										
										/*else{
											message.setFlag(Flags.Flag.SEEN, false);
										}*/
									}

									System.out.println(list);

									if(list.contains("automatic-action/MDN-sent-automatically;processed") && list.contains("automatic-action/MDN-sent-automatically;dispatched")){

										ZonedDateTime endTime = ZonedDateTime.now();
										result.putAll(buffer);
										duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
										result.put("\nElapsed Time", duration.toString().substring(3)+"\n");
									}


								}

							}

						}

					}
				}

			}


			store.close();

			if (result.size() == 0) {
				tr.setCriteriamet(CriteriaStatus.STEP2);
				tr.getTestRequestResponses().put("ERROR","No messages found with Message ID: " + id);
			}

			else if(timeout!=null && (timeout.toMinutes() > timeoutConstant)){

				tr.setCriteriamet(CriteriaStatus.FALSE);
				tr.getTestRequestResponses().put("ERROR","MDN received after timeout");

			}
			
			else if (headerFlag == 1){
				tr.setCriteriamet(CriteriaStatus.FALSE);
				tr.getTestRequestResponses().put("\n"+"ERROR","Dispatched MDN contains X-DIRECT-FINAL-DESTINATION-DELIVERY header");
			}

			else {
				tr.setCriteriamet(CriteriaStatus.TRUE);
			}

		} catch (Exception e) {
			tr.setCriteriamet(CriteriaStatus.FALSE);
			e.printStackTrace();
			log.info("Error fetching email " + e.getLocalizedMessage());
			tr.getTestRequestResponses().put("1","Error fetching email :" + e.getLocalizedMessage());
		}

		tr.setMessageId(id);
		tr.setFetchType(fetch);
		tr.setSearchType(type);
		tr.setStartTime(startTime);
		return tr;
	}
}