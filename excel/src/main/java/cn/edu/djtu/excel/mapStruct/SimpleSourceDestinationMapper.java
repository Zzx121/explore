package cn.edu.djtu.excel.mapStruct;

import org.mapstruct.Mapper;

/**
 * @author zzx
 * @date 2020/10/15
 */
@Mapper
public interface SimpleSourceDestinationMapper {
    SimpleDestination sourceToDestination(SimpleSource source);
    SimpleSource destinationToSource(SimpleDestination destination);
}
