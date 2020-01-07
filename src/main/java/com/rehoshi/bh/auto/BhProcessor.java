package com.rehoshi.bh.auto;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.io.IOUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BhProcessor extends AbstractProcessor {
    boolean created = false;
    Map<String, TypeSpec.Builder> clsNameCache = new LinkedHashMap<>();
    Filer filer;
    File cacheRes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        created = false;
        try {
            FileObject resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "cache.json");
            File file = new File(resource.toUri());
            if (!file.exists()) {
                file.createNewFile();
            }
            cacheRes = file;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (!created) {
                Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HakaiId.class);
                TypeSpec.Builder idBuilder = TypeSpec.classBuilder("Id")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                Set<HakaiInfo> hakaiInfo = getHakaiInfo(elements);
                for (HakaiInfo e : hakaiInfo) {
                    String clsName = e.getClassName();

                    //把class转为内部类
                    TypeSpec.Builder clsBuilder = clsNameCache.get(clsName);
                    if (clsBuilder == null) {
                        clsBuilder = TypeSpec.classBuilder(clsName).addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                        clsNameCache.put(clsName, clsBuilder);
                    }

                    String methodName = e.getMethodName();
                    //将方法名称转为对应字段
                    clsBuilder.addField(FieldSpec.builder(TypeName.LONG, methodName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .addJavadoc("@see $L#$L()", e.getFullClassName(), e.getMethodName())
                            .initializer("$LL", getId())
                            .build());
                }

                //添加所有的class
                for (TypeSpec.Builder builder : clsNameCache.values()) {
                    idBuilder.addType(builder.build());
                }

                TypeSpec typeBuilder = TypeSpec.classBuilder("Hakai")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addType(idBuilder.build())
                        .build();

                JavaFile javaFile = JavaFile.builder("com.rehoshi.bh.auto", typeBuilder)
                        .addFileComment(this.hashCode() + "")
                        .build();
                javaFile.writeTo(filer);
                IOUtils.write(new Gson().toJson(hakaiInfo), new FileOutputStream(cacheRes), "UTF-8");
                created = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Set<HakaiInfo> getHakaiInfo(Set<? extends Element> elements){
        Set<HakaiInfo> infoSet = null;
        try {
            String s = IOUtils.toString(new FileInputStream(cacheRes), StandardCharsets.UTF_8);
            infoSet = new Gson().fromJson(s, new TypeToken<LinkedHashSet<HakaiInfo>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (infoSet == null) {
            infoSet = new LinkedHashSet<>();
        }

        for (Element element : elements){
            String pkgName = element.getEnclosingElement().getEnclosingElement().toString() ;
            String clsName = element.getEnclosingElement().getSimpleName().toString();
            String methodName = element.getSimpleName().toString();
            HakaiInfo hakaiInfo = new HakaiInfo(clsName, methodName) ;
            hakaiInfo.setPkgName(pkgName);
            infoSet.add(hakaiInfo) ;
        }

        return infoSet ;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(HakaiId.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private long getId() {
        return System.nanoTime();
    }
}
