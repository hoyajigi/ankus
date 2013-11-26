/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ankus.mapreduce.algorithms.clustering.em;

import java.io.IOException;
import java.util.Iterator;

import org.ankus.util.ArgumentsConstants;
import org.ankus.util.CommonMethods;
import org.ankus.util.ConfigurationVariable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class EMClusterUpdateReducer extends Reducer<IntWritable, Text, NullWritable, Text>{

	String m_delimiter;
	
	int m_indexArr[];	
	int m_nominalIndexArr[];
	int m_exceptionIndexArr[];
	
	int m_clusterCnt;
	
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException 
	{

	}

//	@Override
	protected void reduce(IntWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException 
	{
		Iterator<Text> iterator = values.iterator();
				
		EMClusterInfoMgr cluster = new EMClusterInfoMgr();
		cluster.setClusterID(key.get());
		int dataCnt = 0;
		while (iterator.hasNext())
		{
			dataCnt++;
			String tokens[] = iterator.next().toString().split(m_delimiter);
			
			for(int i=0; i<tokens.length; i++)
			{
				if(CommonMethods.isContainIndex(m_indexArr, i, true)
						&& !CommonMethods.isContainIndex(m_exceptionIndexArr, i, false))
				{
					if(CommonMethods.isContainIndex(m_nominalIndexArr, i, false))
					{
						cluster.addAttributeValue(i, tokens[i], ConfigurationVariable.NOMINAL_ATTRIBUTE);
					}
					else cluster.addAttributeValue(i, tokens[i], ConfigurationVariable.NUMERIC_ATTRIBUTE);
				}
			}
		}
		cluster.finalCompute(dataCnt);
		
		String writeStr = cluster.getClusterInfoString(m_delimiter, context.getConfiguration().get("subDelimiter", "@@"));
        context.write(NullWritable.get(), new Text(writeStr));
	}

	@Override
	protected void setup(Context context)
			throws IOException, InterruptedException 
	{
		Configuration conf = context.getConfiguration();
		
		m_delimiter = conf.get(ArgumentsConstants.DELIMITER, "\t");
		
		m_indexArr = CommonMethods.convertIndexStr2IntArr(conf.get(ArgumentsConstants.TARGET_INDEX,  "-1"));
		m_nominalIndexArr = CommonMethods.convertIndexStr2IntArr(conf.get(ArgumentsConstants.NOMINAL_INDEX,  "-1"));
		m_exceptionIndexArr = CommonMethods.convertIndexStr2IntArr(conf.get(ArgumentsConstants.EXCEPTION_INDEX,  "-1"));
		
		m_clusterCnt = Integer.parseInt(conf.get(ArgumentsConstants.CLUSTER_COUNT, "1"));
	}
}
