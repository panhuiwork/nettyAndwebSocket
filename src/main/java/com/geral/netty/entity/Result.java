package com.geral.netty.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer code;
	private String msg;
	private Object obj;
	
}
