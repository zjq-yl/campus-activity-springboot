package edu.campus.entity;

public enum PointCategory {
  IDEOLOGY("思想成长"), PRACTICE("实践实习"), VOLUNTEERING("志愿公益"), INNOVATION("创新创业"), CULTURE_SPORTS("文体活动"), LEADERSHIP("社会工作"), SKILLS("技能特长");

  private final String label;
  PointCategory(String label) { this.label=label; }
  public String label() { return label; }
}
