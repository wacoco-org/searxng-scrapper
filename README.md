# SearXNG 

![image](https://github.com/user-attachments/assets/bd16bf31-2855-4528-b265-c193540f2f91)

## What is it?

SearXNG is an **open-source, self-hosted metasearch engine** that aggregates search results from multiple providers
(Google, Bing, DuckDuckGo, etc.) while **ensuring privacy**. Unlike Google, 
SearXNG does **not track user data, show ads, or personalize search results**.It can be **self-hosted** 
and configured to scrape various search engines while bypassing tracking and filtering mechanisms.

---
![image](https://github.com/user-attachments/assets/01a36261-340e-4b9b-89d5-e1660644efda)


---

## How SearXNG Compares to Google

| Feature            | **SearXNG** (Metasearch Engine)                                  | **Google** (Standalone Search Engine)                      |
|--------------------|----------------------------------------------------------------|-----------------------------------------------------------|
| **Indexing**       | Does **not** have its own search index, fetches from other engines. | Uses its own massive search index built by web crawlers.   |
| **Privacy**        | Fully private, does not track users.                           | Tracks user activity, personalizes results based on history. |
| **Customization**  | Users can choose which search engines to use.                 | No customization—Google controls all indexing & ranking.  |
| **Search Sources** | Uses **multiple search engines** like Bing, DuckDuckGo, Wikipedia, etc. | Uses **only Google's own index** and ranking algorithms. |
| **Filtering & Ranking** | Merges results from different engines, applies simple ranking. | Uses complex AI-driven ranking, personalization, and ad-based ranking. |
| **Ads & Monetization** | No ads, open-source, free to use.                           | Google makes money from **ads & personalized search results**. |
| **Self-Hosting**   | Can be **self-hosted** on your own server for full control.   | Google is **controlled by Google servers** and not customizable. |
| **API Support**    | Provides a public and private **search API**.                 | Google has a **paid API (Google Search API)**. |

---

##  How SearXNG Can Be Used for Scraping
Since SearXNG aggregates results from multiple sources, it can be **used as a scraper** to extract search results
programmatically.

by default scrapping isnt allowed, but you can easly override that by modifying settings.yml and add

```
server:
   limiter: false
   method: "GET"

search:
 formats:
   - html
   - json
```

### Why Use SearXNG for Scraping?
- **Avoid API fees** (Google and other scrapper providers charges for their Search API).
- **Bypass rate limits** by switching between multiple engines.
- **Extract data from different sources** in a single request.
- **Customize the scraping process** with different query parameters.


---

## Can You Scrape Unlimited Data?
Well, By default, SearXNG changes the User-Agent and request headers on every request to make it look like different
browsers.
However, it does not change the IP address unless a proxy or Tor is configured.!
**If SearXNG rotates both the IP address and user-agent (browser identity), 
it becomes much harder for search engines to detect that requests are automated.** 
This would **reduce the chance of getting blocked** and could, in theory, allow **unlimited scraping**.


---

## IP Rotation: 

SearXNG **does not rotate IPs by default**, meaning all requests come from the same server.
Search engines **track IPs and can block your requests** if you scrape too aggressively.

I personly tested searching over 250 keywords and got total 3 732 url links using same ip, and I have not encountered any problem.

so **there are 3 ways to Rotate IP Addresses in SearXNG**

* **Using Tor (Free but Slow, Many Sites Block It)**
* Edit settings.yml

```
     outgoing:
      proxies:
       all://:
         - socks5h://127.0.0.1:9050  # Route traffic through Tor
```
* **Using Paid Proxy Services (Fast & Reliable, Costs Money)**
  * Edit settings.yml with rotating proxy provider:

```
      outgoing:
       proxies:
        all://:
         - http://username:password@proxy1.com:8000
         - socks5://proxy2.com:1080
```

* **Create you own proxy (free)** 
  * there are several open-source options to create your own proxy for free



# App

The application is built using Spring WebFlux, which is non-blocking and reactive.
WebClient is used instead of RestTemplate to make asynchronous HTTP requests.

**This App can be deployed as a standalone service that serves search results via**

```
GET /api/v0/search?keyword={your_query}&narrowing={optional_domain}

```
the api takes 2 request param, a keyword and a site, site is optional param, its used to narrow the
search result, for example if you search a name, and you want to narrow the search result, 
you use site, Github for example

```
GET http://localhost:8081/api/v0/search?keyword=java&narrowing=github.com

```
now the search result will only contain github domain

![image](https://github.com/user-attachments/assets/ce603e18-2ef4-466c-bc02-d967af2383c9)

when using the api. response looks like this

```
[
    {
        "title": "java · GitHub Topics",
        "url": "https://github.com/topics/java",
        "description": "GitHub is where people build software. More than 100 million people use GitHub to discover,
         fork, and contribute to over 420 million projects. ... desktop, mobile, and embedded applications. 
         Java is owned and licensed through Oracle, with free and open source implementations available from Oracle 
         and other vendors. Here are 255,331 public ...",
        "engines": [
            "google",
            "duckduckgo"
        ]
    },
    {
        "title": "Java Language Support for Visual Studio Code",
        "url": "https://github.com/redhat-developer/vscode-java",
        "description": "Java 21 is the minimum required version. The path to the Java Development Kit can be specified
         by the java.jdt.ls.java.home setting in VS Code settings ( ...",
        "engines": [
            "google"
        ]
    },
    
```

## SearXNG Query Parameter Breakdown

This table is detailed Breakdown on SearXNG Query  that exist in the code.

| Parameter      | Value                                     | Explanation                                      |
|-------------- |-----------------------------------------|--------------------------------------------------|
| **q**         | `site:example.com your search query`   | Limits search to `example.com`.                 |
| **categories** | `general`                              | No change; searches all general results.        |
| **language**   | `auto`                                 | Auto-detects language.                          |
| **time_range** | *(empty)*                              | No time restriction.                            |
| **safesearch** | `0`                                   | Disables SafeSearch (shows all results).        |
| **theme**     | `simple`                               | Keeps UI minimalistic.                          |



    
<hr/>


more about searxng source code 


* for docker

 https://github.com/searxng/searxng-docker

* source repo

https://github.com/searxng/searxng

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven**
- **IntelliJ IDEA** (Recommended)
- **Docker**
 
