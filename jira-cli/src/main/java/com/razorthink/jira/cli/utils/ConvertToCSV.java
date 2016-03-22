package com.razorthink.jira.cli.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.google.common.primitives.Primitives;
import com.razorthink.jira.cli.exception.DataException;

/**
 * 
 * @author reshma
 *
 */
public class ConvertToCSV {

	private static final Logger logger = LoggerFactory.getLogger(ConvertToCSV.class);

	public <T> String getFileHeader( Field[] fields )
	{
		String FILE_HEADER = "";
		int i = 0;
		for( Field field : fields )
		{
			// get the static type of the field
			Class<?> fieldType = field.getType();
			if( Primitives.isWrapperType(fieldType) || fieldType == String.class )
			{
				FILE_HEADER += field.getName();
				i++;
				if( i < fields.length )
				{
					FILE_HEADER += ",";
				}
			}
			else if( fieldType == String[].class )
			{
				FILE_HEADER += field.getName();
				i++;
				if( i < fields.length )
				{
					FILE_HEADER += ",";
				}
			}
			else if( fieldType == List.class )
			{
				FILE_HEADER += field.getName();
				i++;
				if( i < fields.length )
				{
					FILE_HEADER += ",";
				}
			}
			else if( Object.class.isAssignableFrom(fieldType) )
			{
				Field[] subFields = fieldType.getDeclaredFields();
				for( Field subField : subFields )
				{
					FILE_HEADER += subField.getName();
					FILE_HEADER += ",";
				}
			}
		}
		return FILE_HEADER;
	}

	public <T> void exportToCSV( String csvFileName, List<T> objects )
	{
		// CSV file header
		String FILE_HEADER = "";
		String COMMA_DELIMITER = ",";
		String NEW_LINE_SEPARATOR = "\n";
		if( objects.isEmpty() )
		{
			return;
		}
		Field[] fields = objects.get(0).getClass().getDeclaredFields();
		FILE_HEADER = getFileHeader(fields);
		FileWriter fileWriter = null;

		try
		{
			fileWriter = new FileWriter(csvFileName);
			fileWriter.append(FILE_HEADER.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			for( T object : objects )
			{
				try
				{
					for( Field field : fields )
					{
						field.setAccessible(true);
						Class<?> fieldType = field.getType();

						if( Primitives.isWrapperType(fieldType) || fieldType == String.class )
						{
							Object value = field.get(object);
							if( value != null )
							{
								String escaped = StringEscapeUtils.escapeCsv(value.toString());
								fileWriter.append(escaped);
							}
							else
								fileWriter.append("null");
							fileWriter.append(COMMA_DELIMITER);
						}
						else if( fieldType == String[].class )
						{
							Object value = field.get(object);
							if( value != null )
								fileWriter.append(value.toString());
							else
								fileWriter.append("null");
							fileWriter.append(COMMA_DELIMITER);
						}
						else if( fieldType == List.class )
						{
							Object value = field.get(object);
							if( value != null )
								fileWriter.append(value.toString());
							else
								fileWriter.append("null");
							fileWriter.append(COMMA_DELIMITER);
						}
						else if( Object.class.isAssignableFrom(fieldType) )
						{
							Field[] subFields = fieldType.getDeclaredFields();
							Object value2 = field.get(object);
							for( Field subField : subFields )
							{
								subField.setAccessible(true);
								Object value1 = subField.get(value2);
								if( value1 != null )
								{
									fileWriter.append(value1.toString());
								}
								else
									fileWriter.append("null");
								fileWriter.append(COMMA_DELIMITER);
							}
						}
					}
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				catch( SecurityException | IllegalArgumentException | IllegalAccessException e )
				{
					logger.error(e.getMessage());
					throw new DataException("500", HttpStatus.INTERNAL_SERVER_ERROR.name());
				}
			}
		}
		catch( IOException e )
		{
			logger.error(e.getMessage());
			throw new DataException("500", HttpStatus.INTERNAL_SERVER_ERROR.name());
		}
		finally
		{
			try
			{
				fileWriter.flush();
				fileWriter.close();
			}
			catch( IOException e )
			{
				logger.error(e.getMessage());
				throw new DataException("500", HttpStatus.INTERNAL_SERVER_ERROR.name());
			}
		}
	}
}
