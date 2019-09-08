package com.ota.updates.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

import com.ota.updates.RomUpdate;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Utils;

public class RomXmlParser extends DefaultHandler implements Constants {
	public final String TAG = "OTATAG";
	private StringBuffer value = new StringBuffer();
	private Context mContext;
	private boolean tagRomName = false;
	private boolean tagVersionName = false;
	private boolean tagVersionNumber = false;
	private boolean tagDirectUrl = false;
	private boolean tagHttpUrl = false;
	private boolean tagMD5 = false;
	private boolean tagLog = false;
	private boolean tagWebsite = false;
	private boolean tagFileSize = false;
	private boolean tagUpdateDate = false;

	public void parse(File xmlFile, Context context) throws IOException {
		mContext = context;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlFile, this);

			Utils.setUpdateAvailability(context);

		} catch (ParserConfigurationException ex) {
			Log.e(TAG, "", ex);
		} catch (SAXException ex) {
			Log.e(TAG, "", ex);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		
		value.setLength(0);

		if (attributes.getLength() > 0) {

			@SuppressWarnings("unused")
			String tag = "<" + qName;
			for (int i = 0; i < attributes.getLength(); i++) {

				tag += " " + attributes.getLocalName(i) + "="
						+ attributes.getValue(i);
			}
			tag += ">";
		}

		if (qName.equalsIgnoreCase("name")) {
			tagRomName = true;
		}

		if (qName.equalsIgnoreCase("version_name")) {
			tagVersionName = true;
		}
		
		if (qName.equalsIgnoreCase("version_number")) {
			tagVersionNumber = true;
		}

		if (qName.equalsIgnoreCase("drct_url")) {
			tagDirectUrl = true;
		}

		if (qName.equalsIgnoreCase("http_url")) {
			tagHttpUrl = true;
		}

		if (qName.equalsIgnoreCase("md5")) {
			tagMD5 = true;
		}
		
		if (qName.equalsIgnoreCase("size")) {
			tagFileSize = true;
		}

		if (qName.equalsIgnoreCase("site_url")) {
			tagWebsite = true;
		}
		
		if (qName.equalsIgnoreCase("changes")) {
			tagLog = true;
		}

		if (qName.equalsIgnoreCase("update_date")) {
			tagUpdateDate = true;
		}

	}

	@Override
	public void characters(char[] buffer, int start, int length) 
			throws SAXException{
		value.append(buffer, start, length);;
		
	}    

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		String input = value.toString().trim();
		
		if (tagRomName) {
			RomUpdate.setRomName(mContext, input);
			tagRomName = false;
			if (DEBUGGING)
				Log.d(TAG, "Name = " + input);
		}

		if (tagVersionName) {
			RomUpdate.setVersionName(mContext, input);
			tagVersionName = false;
			if (DEBUGGING)
				Log.d(TAG, "Version = " + input);
		}
		
		if (tagVersionNumber) {
			RomUpdate.setVersionNumber(mContext, input);
			tagVersionNumber = false;
			if (DEBUGGING)
				Log.d(TAG, "OTA Version = " + input);
		}

		if (tagDirectUrl) {
			if (!input.isEmpty()) {
				RomUpdate.setDirectUrl(mContext, input);
				setUrlDomain(input);
			} else {
				RomUpdate.setDirectUrl(mContext, "null");
			}
			RomUpdate.setDirectUrl(mContext, input);
			tagDirectUrl = false;
			if (DEBUGGING)
				Log.d(TAG, "URL = " + input);
		}

		if (tagHttpUrl) {
			if (!input.isEmpty()) {
				RomUpdate.setHttpUrl(mContext, input);
				setUrlDomain(input);
			} else {
				RomUpdate.setHttpUrl(mContext, "null");
			}
			tagHttpUrl = false;
			if (DEBUGGING)
				Log.d(TAG, "HTTP URL = " + input);
		}

		if (tagMD5) {
			RomUpdate.setMd5(mContext, input);
			tagMD5 = false;
			if (DEBUGGING)
				Log.d(TAG, "MD5 = " + input);
		}
		
		if (tagFileSize) {
			RomUpdate.setFileSize(mContext, Integer.parseInt(input));
			tagFileSize = false;
			if (DEBUGGING)
				Log.d(TAG, "Filesize = " + input);
		}


		if (tagWebsite) {
			if (!input.isEmpty()) {
				RomUpdate.setWebsite(mContext, input);
			} else {
				RomUpdate.setWebsite(mContext, "null");
			}
			tagWebsite = false;
			if (DEBUGGING)
				Log.d(TAG, "Website = " + input);
		}
		
		if (tagLog) {
			RomUpdate.setChangelog(mContext, input);
			tagLog = false;
			if (DEBUGGING)
				Log.d(TAG, "Changelog = " + input);
		}

		if (tagUpdateDate) {
			RomUpdate.setUpdateDate(mContext, input);
			tagUpdateDate = false;
			if (DEBUGGING)
				Log.d(TAG, "Update date = " + input);
		}

	}
	
	private void setUrlDomain(String input) {
		URI uri;
		try {
			uri = new URI(input);
			String domain = uri.getHost();
			RomUpdate.setUrlDomain(mContext, domain.startsWith("www.") ? domain.substring(4) : domain);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			RomUpdate.setUrlDomain(mContext, "Error");
		}
	    
		
	}
}

