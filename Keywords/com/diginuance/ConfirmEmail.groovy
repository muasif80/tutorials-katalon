package com.diginuance

import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.NoSuchProviderException
import javax.mail.Session
import javax.mail.Store
import javax.mail.Message.RecipientType
import javax.mail.search.AndTerm
import javax.mail.search.RecipientStringTerm
import javax.mail.search.SearchTerm
import javax.mail.search.SubjectTerm

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable

public class ConfirmEmail {

	@Keyword
	def fetchEmailVerifyURL() {
		
		String url = findConfirmEmailURL()
		WebUI.navigateToUrl(url)								// Navigate to the URL retrieved
	}
		
	// Mail related methods
	private String findConfirmEmailURL() throws MessagingException {
		
		SearchTerm filter = createFilter();						// 1. Create mail search filter for our mailbox
		Store store = connect();								// 2. Connect to our mailbox
		String content = getMessageContent(store, filter);		// 3. Retrieve the message body from the received email
		String url = getURL(content)							// 4. Get the confirmation URL from the message body
		return url;
	}
	
	
	private SearchTerm createFilter(){
		SearchTerm t1 = new RecipientStringTerm(RecipientType.TO, GlobalVariable.G_Email_To)
		SearchTerm t2 = new SubjectTerm(GlobalVariable.G_Email_Subject)
		SearchTerm st = new AndTerm(t1, t2);
		return st;
	}
	
	private Store connect() throws MessagingException {
		
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		
		Session session = Session.getDefaultInstance(props, null);
		Store store;
		
		try{
			
			store = session.getStore("imaps");
			store.connect(GlobalVariable.G_Host_Imap, GlobalVariable.G_Email_Username, GlobalVariable.G_Email_Password);
			
		}catch (NoSuchProviderException e) {
			
			e.printStackTrace();
			throw e;
		}catch (MessagingException e) {
			
			e.printStackTrace();
			throw e;
		}

		return store;
	}
	
	public String getMessageContent(Store store, SearchTerm filter) throws MessagingException {
		
		String mailMessageContent = "";
		try{
			
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_ONLY);

			Message[] messages = emailFolder.search(filter);
			System.out.println("messages.length---" + messages.length);
			
			if(messages.length != 0){
			
				Message message = messages[0];
				
				String subject = message.getSubject();
				String from = message.getFrom()[0].toString();
				String content = message.getContent().toString();
				
				mailMessageContent = content;
			}
			
			emailFolder.close(false);
		} catch (MessagingException e) {
		
			e.printStackTrace();
		} catch (Exception e) {
		
			e.printStackTrace();
		}finally {
		
			store.close();
		}
		
		return mailMessageContent;
	}
	
	private String getURL(String content){
		Document doc = Jsoup.parse(content);
		//Element element = doc.selectFirst(GlobalVariable.G_Confirm_Url_Search_Expr);
		Element element = doc.getElementsByAttributeValueContaining("href", GlobalVariable.G_Confirm_Url_Part)[0]
		return element.attr("href");
	}
}
