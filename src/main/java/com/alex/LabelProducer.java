package com.alex;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LabelProducer {
    private static final Configuration cfg;
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelProducer.class);

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(LabelProducer.class, "/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    public static void main(String[] args) {

        try {
            if (args.length < 2)
                throw new Exception("Ошибка ввода параметров этикетки: должно быть два параметра (ШК " +
                        "и наименование товара)");
            Template template = cfg.getTemplate("label.ftl");
            Map<String, String> data = new HashMap<>();
            data.put("code", args[0]);
            data.put("title", args[1]);
            try (FileWriter writer = new FileWriter(new File("barcode.zpl"))) {
                template.process(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Шаблон успешно создан");
    }
}
