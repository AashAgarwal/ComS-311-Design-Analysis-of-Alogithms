/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author agarwal
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;



public class WikiCrawler {
	private String SEED;
	private static int MAX;
	private static String OUTPUT;
	private final static String BASE_URL = "http://web.cs.iastate.edu/~pavan";
	private static ArrayList<String> TOPICS = new ArrayList<>();
	private static int counter;

	public WikiCrawler(String seed, int max,String[] topics, String output)
	{
		this.SEED=seed;
		this.MAX=max;
		this.counter=0;
		this.TOPICS=new ArrayList<>(Arrays.asList(topics));
		this.OUTPUT=output;
	}

	public static void getHtmlPage(String complete_URL){

		StringBuilder contentBuilder = new StringBuilder();
		try {
			URL wiki = new URL(complete_URL);
			BufferedReader in = new BufferedReader(new InputStreamReader(wiki.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				contentBuilder.append(str);
			}
			in.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		String document = contentBuilder.toString();
		extractLinks(complete_URL,document);
	}

	private static String normalizeUrlStr(String urlStr) {
		if (!urlStr.startsWith("http")) {
			urlStr =  BASE_URL  + urlStr;
		}
		if (urlStr.endsWith("/")) {
			urlStr = urlStr.substring(0, urlStr.length() - 1);
		}
		if (urlStr.contains("#")) {
			urlStr = urlStr.substring(0, urlStr.indexOf("#"));
		}
		return urlStr;
	}

	public static void extractLinks(String url,String document)
	{
		ArrayList<String> topics = new ArrayList<>();
		try{
			String input;
			BufferedReader in = new BufferedReader(new StringReader(document));
			while ((input = in.readLine()) != null)
			{
				//System.out.println(input);
				Pattern p = Pattern.compile("<a href=\"(.*?)\"", Pattern.DOTALL);
				Matcher m = p.matcher(input);
				while (m.find()) {
					System.out.println(m.group(1));
					String linkStr = normalizeUrlStr(m.group(1));
					try {
						URL link = new URL(linkStr);
						if(!TOPICS.contains(link.toString())){
							topics.add(link.toString());
							TOPICS.add(link.toString());
						}
					}
					catch (MalformedURLException e) {
						System.err.println("Page at " + url + " has a link to invalid URL : " + linkStr + ".");
					}
				}

			}
			in.close();
			optext(url,topics);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	private static void optext(String url,ArrayList<String> topics)
	{
		try(FileWriter file = new FileWriter(OUTPUT, true);
				BufferedWriter buff = new BufferedWriter(file);
				PrintWriter prw = new PrintWriter(buff))
		{

			if(topics.isEmpty())
			{
				String link = url;
				prw.println(url.replace(BASE_URL,"") + " " + link.replace(BASE_URL,""));

			}
			else
			{
				int z=0;
				int left = MAX - counter;
				if(left>=topics.size()){
					z=topics.size();
					counter+=z;
				}
				else{
					z=left;
					counter=MAX;
				}
				for(int i=0; i<z; i++)
				{
					String link = topics.get(i);
					prw.println(url.replace(BASE_URL,"") + " " + link.replace(BASE_URL,""));

				}
			}
		}catch (IOException e) {
		}
		for(int i=0; i<topics.size(); i++)
		{
			String link = topics.get(i);
			//System.out.println(link);
			if (counter % 20 == 0) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println(counter);
			if (counter < MAX ) {
				getHtmlPage(link);
			}
			else{
				break;
			}
		}

	}

	public void crawl(boolean focussed) {
		try(FileWriter file = new FileWriter(OUTPUT, true);
			BufferedWriter buff = new BufferedWriter(file);
			PrintWriter prw = new PrintWriter(buff))
		{
			prw.println(MAX);

		}catch (IOException e) {
		}
		String new_link=this.SEED;
		String complete_URL=BASE_URL+new_link;
		TOPICS.add(complete_URL);
		getHtmlPage(complete_URL);
	}

}
