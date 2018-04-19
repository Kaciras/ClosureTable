package net.kaciras.example;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 分类实体对象
 *
 * @author kaciras
 */
@EqualsAndHashCode(of = "id")
@Data
public class Category {

	/** 分类id，数据库生成 */
	private int id;

	/** 分类名 */
	private String name;

	/** 自定义字段，封面 */
	private String cover;

	/** 自定义字段，描述 */
	private String description;
}
