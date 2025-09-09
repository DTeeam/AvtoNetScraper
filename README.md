# AvtoNetScraper

A Java Spring Boot application that integrates with Telegram to scrape and manage Avtonet ads. The project uses Maven for build management and JPA for persistence.

## Features

- Telegram Bot integration
- Avtonet results and ads management
- JPA/Hibernate persistence
- RESTful API endpoints

## Technologies

- Java
- Spring Boot
- Maven
- JPA (Hibernate)
- Telegram Bot API

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Telegram Bot Token

### Setup

1. Clone the repository:

2. Configure your Telegram Bot token in `application.properties`:

3. Build and run the application:

## Usage

- Interact with the bot via Telegram.
- Send URL (http...)  for a search query (Ads/result) or for a specific ad (Ads/details).
- /unsub <url> – unsubscribe from a specific search query or ad.
 - /help – shows this message.

## Disclaimer

This project is intended solely for educational and research purposes (bachelor's thesis).
Mass scraping may violate the terms of service of websites – use responsibly.

## License

This project is licensed under the MIT License.
