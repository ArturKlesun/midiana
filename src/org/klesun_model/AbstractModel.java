
package org.klesun_model;

// AbstractModel is an IModel with attached Helper instance the Helper
// is used to store field list, since an interface can't have properties...

// TODO: probably no need for Helper anymore - move the fieldStorage here

import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.klesun_model.field.IField;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public abstract class AbstractModel implements IModel {

	protected Helper h = new Helper(this);

	private Map<String, IField> fieldStorage = new TreeMap<>();

	@Override
	public String toString() {
		return this.getJsonRepresentation().toString();
	}

	// the name because it can't guess generics when call in call
	private <E> Field<E> addIHateJava(String name, Field<E> field) {
		fieldStorage.put(name, field);
		return field;
	}

	/** mutable */
	protected <E> Field<E> add(String name, E value) {
		return addIHateJava(name, new Field<>(value));
	}

	protected <E> Field<E> add(String name, E value, Function<E,E> normalize) {
		return addIHateJava(name, new Field<>(value, normalize));
	}

	/** final */
	protected <E> Field<E> add(String name, Class<E> elemClass) {
		return addIHateJava(name, new Field<>(elemClass));
	}

	protected <E extends IModel> Arr<E> add(String name, Collection<E> container, Class<E> elemClass) {
		Arr<E> field = new Arr<>(container, elemClass);
		fieldStorage.put(name, field);
		return field;
	}

	public Map<String, IField> getFieldStorage() {
		return fieldStorage;
	}
}
