import styled from 'styled-components'
import Colors from 'Config/Colors'

export const StatisticsMainFormStyle = styled.div `
    .title{
        padding-left:40px;
        padding-top:20px;
        font-size: 40px;
        margin-bottom: 25px;
    }

    .data-field{
        width: 100%;
        height: 100px;
    }

    .buttom-field{
        width: 100%;
        height: 100px;
    }

    .buttonSearch{
        float:right;
        margin-right:40px;
        font-size:20px;
        background-color: ${Colors.contrast_1};
        color: ${Colors.primary};
        border: 0;
    }
`