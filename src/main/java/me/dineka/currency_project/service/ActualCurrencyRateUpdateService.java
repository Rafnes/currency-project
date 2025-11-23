package me.dineka.currency_project.service;

import lombok.extern.slf4j.Slf4j;
import me.dineka.currency_project.exception.ExchangeRateUpdateException;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import me.dineka.currency_project.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ActualCurrencyRateUpdateService {
    private final CurrencyRepository currencyRepository;

    public ActualCurrencyRateUpdateService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public void updateRates() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String address = "https://cbr.ru/scripts/XML_daily.asp?date_req=" + LocalDateTime.now().format(formatter);

        URL url = new URL(address);
        StringBuilder data = new StringBuilder();
        log.info("Чтение данных с сайта ЦБ");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        }

        XPath xpath = null;
        NodeList nodes = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(data.toString())));

            xpath = XPathFactory.newInstance().newXPath();
            nodes = (NodeList) xpath.evaluate("/ValCurs/Valute", doc, XPathConstants.NODESET);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            log.error("Ошибка чтения данных курсов валют: {}", e.getMessage());
            throw new ExchangeRateUpdateException("Не удалось прочитать курсы валют: " + e.getMessage());
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            String code = null;
            Double rate = null;
            Integer nominal = null;
            try {
                Node currencyNode = nodes.item(i);
                code = xpath.evaluate("CharCode", currencyNode);
                String valueStr = xpath.evaluate("Value", currencyNode).replace(',', '.');
                String nominalStr = xpath.evaluate("Nominal", currencyNode);

                rate = Double.parseDouble(valueStr);
                nominal = Integer.parseInt(nominalStr);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }

            Currency currency = currencyRepository.findByCodeIgnoreCase(code).orElse(null);
            if (currency != null) {
                log.info("Обнаружена добавленная валюта: {}", currency.getCode());
                ExchangeRate exchangeRate = currency.getExchangeRate();
                if (!exchangeRate.getRate().equals(rate)) {
                    exchangeRate.setRate(rate);
                    exchangeRate.setUpdatedAt(LocalDateTime.now());
                    currency.setNominal(nominal);
                    currencyRepository.save(currency);
                    log.info("Обновлен курс {} с сайта ЦБ: курс: {}, номинал: {}", currency.getCode(), currency.getExchangeRate(), currency.getNominal());
                } else {
                    log.info("У валюты {} актуальный курс, обновление не требуется", currency.getCode());
                }
            }
        }
    }
}
