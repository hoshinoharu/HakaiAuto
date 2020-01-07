package com.rehoshi.bh.auto;

public class HakaiInfo {
    private String className ;
    private String methodName ;
    private String pkgName ;

    public HakaiInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public int hashCode() {
        return className.hashCode() + methodName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HakaiInfo){
            HakaiInfo that = (HakaiInfo) obj;
            return this.methodName.equals(that.methodName) && this.className.equals(that.className) ;
        }
        return false ;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getFullClassName(){
        return getPkgName() + "." + getClassName() ;
    }
}
