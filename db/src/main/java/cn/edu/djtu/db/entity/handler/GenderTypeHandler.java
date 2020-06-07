package cn.edu.djtu.db.entity.handler;

import cn.edu.djtu.db.entity.Gender;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName GenderTypeHandler
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/8
 **/
@MappedTypes(Gender.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class GenderTypeHandler extends BaseTypeHandler<Gender> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Gender parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public Gender getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return Gender.getGenderByValue(value);
    }

    @Override
    public Gender getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return Gender.getGenderByValue(value);
    }

    @Override
    public Gender getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return Gender.getGenderByValue(value);
    }

}
