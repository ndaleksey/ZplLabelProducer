package com.alex;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class LabelProducer {
    private static final Configuration cfg;
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelProducer.class);
    private static final String LABEL = "label.zpl";

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(LabelProducer.class, "/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    private static void createLabel(String code, String title) throws IOException,
            TemplateException {
        Template template = cfg.getTemplate("label.ftl");
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        data.put("title", title);
        try (FileWriter writer = new FileWriter(new File(LABEL))) {
            template.process(data, writer);
        }
        LOGGER.info("Шаблон успешно создан (label.zpl)");
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2)
                throw new Exception("Ошибка ввода параметров этикетки: должно быть два параметра (ШК " +
                        "и наименование товара)");
            createLabel(args[0], args[1]);

            if (args.length >= 3) {
                if (args.length == 4)
                    sendLabelToPrinter(args[2], args[3]);
                else
                    sendLabelToPrinter(args[2]);
            }
        } catch (NumberFormatException nfe) {
            LOGGER.error("Ошибка ввода порта: " + nfe.getMessage());
        } catch (UnknownHostException uhe) {
            LOGGER.error("Ошибка ввода ip-адреса: " + uhe.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void sendLabelToPrinter(String ip) throws IOException {
        sendLabelToPrinter(ip, "9100");
    }

    private static void sendLabelToPrinter(String ip, String port) throws IOException {
        int ipPort = Integer.parseInt(port);
        if (ipPort < 0 || ipPort > 65535) throw new NumberFormatException("Задан несуществующий " +
                "порт");
        InetAddress ipAddress = InetAddress.getByName(ip);

        LOGGER.info("Отправка этикетки на принтер");

        try (Socket s = new Socket(ipAddress, ipPort)) {
            OutputStream out = s.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            writer.println(getLabelContent());
            writer.flush();
            LOGGER.info("Этикетка отправлена на принтер");
        }
    }

    private static String getLabelContent() throws IOException {
        StringBuilder text = new StringBuilder();

        try (FileReader fReader = new FileReader(new File("label.zpl"));
             BufferedReader bReader = new BufferedReader(fReader)) {
            String line;
            while ((line = bReader.readLine()) != null) {
                text.append(line);
            }

            LOGGER.info(text.toString());
        }

        return text.toString();
    }
}
