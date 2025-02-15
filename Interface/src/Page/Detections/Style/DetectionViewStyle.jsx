import styled from 'styled-components'
import Colors from 'Config/Colors'
export const DetectionViewStyle = styled.div `

    position: relative;
    
    .viewTitle{
        font-size: 25px;
        margin-left:10px;
        margin-bottom:10px;
        margin-top:20px;
        display: flex;
        width: 100%
    }

    .viewCard{
        display:flex;
        flex-wrap: wrap;
    }

    .exploreIcon {
        color: ${Colors.primary};
    }

    .buttonLocal {
        background-color: ${Colors.contrast_1};
        color: ${Colors.primary};
        border: none;
        margin-left: 10px;
    }
`